import pandas as pd
import psycopg2
import os
import sys
from Bio import SeqIO
from psycopg2.extras import execute_values
from tqdm import tqdm

# ================================================================= #
# --- CONFIGURATION ---
# ================================================================= #
DATABASE_URL = "postgresql://neondb_owner:npg_QIM4pDRuBNO6@ep-noisy-night-a1jkftqy.ap-southeast-1.aws.neon.tech/neondb?sslmode=require"

METADATA_PATH = "../databases/USGS_eDNA_Deepsearch_metadata.csv"
PATHS = {
    '18S': {
        'annotated_clusters': "../06_annotation/18S_A/annotated_clusters_18S.csv",
        'seq_table': "../03_dada2_output_18s_A/seqtab.tsv",
        'fasta': "../03_dada2_output_18s_A/asv_sequences.fasta"
    },
    'COI': {
        'annotated_clusters': "../06_annotation/COI/annotated_clusters_COI.csv",
        'seq_table': "../03_dada2_output_coi/seqtab.tsv",
        'fasta': "../03_dada2_output_coi/asv_sequences.fasta"
    }
}
# ================================================================= #

def populate_samples(conn, metadata_df):
    """Populates the Samples table with metadata, optimized for bulk insertion."""
    print("Populating 'Samples' table with full metadata...")
    with conn.cursor() as cur:
        samples_to_insert = []
        for _, row in metadata_df.iterrows():
            collection_date = None
            date_str = row.get('Collection_Date')
            if pd.notna(date_str):
                date_str = str(date_str).strip()
                if len(date_str) == 7 and date_str[4] == '-':
                    collection_date = f"{date_str}-01"
                else:
                    try:
                        collection_date = pd.to_datetime(date_str).strftime('%Y-%m-%d')
                    except ValueError:
                        print(f"Warning: Could not parse malformed date for sample {row.get('NCBI_SRA_Accession')}. Value: '{date_str}'")
            lat_val, lon_val = None, None
            lat_lon_data = row.get('lat_lon')
            if pd.notna(lat_lon_data) and isinstance(lat_lon_data, str) and 'N' in lat_lon_data:
                try:
                    lat_str, lon_str = lat_lon_data.split('N')
                    lon_val_str = lon_str.split('W')[0].strip()
                    lat_val_str = lat_str.strip()
                    if lat_val_str and lon_val_str:
                        lat_val = float(lat_val_str)
                        lon_val = float(lon_val_str)
                except (ValueError, IndexError):
                    print(f"Warning: Could not parse lat_lon for sample {row.get('NCBI_SRA_Accession')}. Value: {lat_lon_data}")

            sample_values = (
                row.get('NCBI_SRA_Accession'),
                collection_date, # Use the corrected date
                lat_val,
                lon_val,
                row.get('Environment_broad_scale'),
                row.get('Environmental_local_scale'),
                row.get('Environmental_medium'),
                row.get('filter_type'),
                row.get('Niskin_name'),
                row.get('target_gene')
            )
            samples_to_insert.append(sample_values)

        if samples_to_insert:
            execute_values(
                cur,
                """
                INSERT INTO Samples (sample_id, collection_date, latitude, longitude,
                                 environment_broad_scale, environmental_local_scale,
                                 environmental_medium, filter_type, niskin_name, target_gene)
                VALUES %s
                ON CONFLICT (sample_id) DO NOTHING;
                """,
                samples_to_insert,
                page_size=1000
            )

    conn.commit()
    print("'Samples' table populated.")


def populate_comprehensive_data(conn, marker, paths):
    """
    Loads, processes, and populates the database for a given marker (18S or COI)
    using efficient bulk operations.
    """
    print(f"\n--- Populating comprehensive data for {marker} ---")

    print("   - Loading data files...")
    annotated_clusters_df = pd.read_csv(paths['annotated_clusters'])
    seq_table_df = pd.read_csv(paths['seq_table'], sep="\t", index_col=0)
    sequences = {record.id: str(record.seq) for record in SeqIO.parse(paths['fasta'], "fasta")}

    with conn.cursor() as cur:
        print("   - Populating 'Clusters' table...")
        cluster_map = {}
        unique_clusters = annotated_clusters_df.drop_duplicates(subset=['Cluster_ID'])
        for _, row in unique_clusters.iterrows():
            cluster_id = int(row['Cluster_ID'])
            cur.execute(
                "INSERT INTO Clusters (marker_type, cluster_id, annotation_name, confidence_level) VALUES (%s, %s, %s, %s) RETURNING cluster_pk_id;",
                (marker, cluster_id, row['scientific_name'], row['confidence_level'])
            )
            cluster_pk_id = cur.fetchone()[0]
            cluster_map[cluster_id] = cluster_pk_id
        print(f"   - Populated {len(cluster_map)} unique clusters.")

        print("   - Preparing ASV data for bulk insert...")
        asv_to_cluster_map = pd.Series(annotated_clusters_df.Cluster_ID.values, index=annotated_clusters_df.ASV_ID).to_dict()

        asvs_to_insert = []
        for asv_hash, sequence in tqdm(sequences.items(), desc="   - Processing ASVs"):
            cluster_id = asv_to_cluster_map.get(asv_hash)
            if cluster_id is not None:
                cluster_pk_id = cluster_map.get(int(cluster_id))
                if cluster_pk_id is not None:
                    asvs_to_insert.append((asv_hash, marker, sequence, cluster_pk_id))

        print(f"   - Populating 'ASVs' table with {len(asvs_to_insert)} records via bulk insert...")
        if asvs_to_insert:
            execute_values(
                cur,
                "INSERT INTO ASVs (asv_hash, marker_type, sequence, cluster_pk_id) VALUES %s ON CONFLICT (asv_hash) DO NOTHING;",
                asvs_to_insert,
                page_size=1000
            )
        print("   - Bulk insert for ASVs complete.")

        print("   - Fetching primary keys for ASVs...")
        all_asv_hashes = [item[0] for item in asvs_to_insert]
        cur.execute("SELECT asv_hash, asv_pk_id FROM ASVs WHERE asv_hash = ANY(%s)", (all_asv_hashes,))
        asv_map = dict(cur.fetchall())
        print(f"   - Mapped {len(asv_map)} ASV primary keys.")

        print("   - Preparing 'Abundance' data for bulk insert...")
        seq_to_hash_map = {seq: asv_hash for asv_hash, seq in sequences.items()}
        
        abundance_to_insert = []
        for sample_id in tqdm(seq_table_df.columns, desc="   - Processing Abundance"):
            sample_data = seq_table_df[sample_id]
            total_reads = sample_data.sum()
            if total_reads == 0:
                continue

            for seq_str, raw_count in sample_data.items():
                if raw_count > 0:
                    asv_hash = seq_to_hash_map.get(seq_str)
                    if asv_hash and asv_hash in asv_map:
                        asv_pk_id = asv_map[asv_hash]
                        relative_abundance = raw_count / total_reads
                        abundance_to_insert.append((sample_id, asv_pk_id, int(raw_count), relative_abundance))

        print(f"   - Populating 'Abundance' table with {len(abundance_to_insert)} records via bulk insert...")
        if abundance_to_insert:
            execute_values(
                cur,
                "INSERT INTO Abundance (sample_id, asv_pk_id, raw_count, relative_abundance) VALUES %s;",
                abundance_to_insert,
                page_size=1000
            )
        print(f"   - Abundance data processed for {len(seq_table_df.columns)} samples.")

    conn.commit()
    print(f"--- {marker} data population complete ---")


def main():
    try:
        conn = psycopg2.connect(DATABASE_URL)
    except psycopg2.OperationalError as e:
        print(f"ERROR: Could not connect. Check DATABASE_URL. Details: {e}")
        return

    with conn.cursor() as cur:
        print("Truncating all tables for a fresh import...")
        cur.execute("TRUNCATE TABLE Abundance, ASVs, Clusters, Samples CASCADE;")
    conn.commit()

    print("Filtering metadata for samples present in sequence tables...")
    metadata = pd.read_csv(METADATA_PATH)
    seq_table_18s_cols = pd.read_csv(PATHS['18S']['seq_table'], sep="\t", index_col=0, nrows=0).columns.tolist()
    seq_table_coi_cols = pd.read_csv(PATHS['COI']['seq_table'], sep="\t", index_col=0, nrows=0).columns.tolist()
    all_samples = set(seq_table_18s_cols + seq_table_coi_cols)
    metadata = metadata[metadata['NCBI_SRA_Accession'].isin(all_samples)]
    # metadata.dropna(subset=['lat_lon'], inplace=True) 
    print(f"Found {len(metadata)} relevant samples in metadata to process.")

    populate_samples(conn, metadata)
    populate_comprehensive_data(conn, '18S', PATHS['18S'])
    populate_comprehensive_data(conn, 'COI', PATHS['COI'])

    conn.close()
    print("\n--- SUCCESS: Comprehensive database population complete. ---")

if __name__ == "__main__":
    main()