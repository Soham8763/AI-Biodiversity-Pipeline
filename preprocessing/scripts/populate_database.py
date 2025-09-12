import pandas as pd
import psycopg2
import os
import sys
# from dotenv import load_dotenv
# load_dotenv()

# ================================================================= #
# --- CONFIGURATION ---
# ================================================================= #
# --- BEST PRACTICE: Load database credentials from an environment variable ---
# This keeps your secret connection string out of the code.
DATABASE_URL = os.environ.get("DATABASE_URL")

METADATA_PATH = "../databases/USGS_eDNA_Deepsearch_metadata.csv"

# Paths to your final output files from the annotation step
ANNOTATED_CLUSTERS_18S_PATH = "../06_annotation/18S_A/annotated_clusters_18S.csv"
SEQ_TABLE_18S_PATH = "../03_dada2_output_18s_A/seqtab.tsv"
ANNOTATED_CLUSTERS_COI_PATH = "../06_annotation/COI/annotated_clusters_COI.csv"
SEQ_TABLE_COI_PATH = "../03_dada2_output_coi/seqtab.tsv"
# ================================================================= #

def populate_samples(conn, metadata_df):
    print("Populating 'Samples' table...")
    with conn.cursor() as cur:
        for index, row in metadata_df.iterrows():
            try:
                # This is a simplified insert; you would add more metadata fields
                lat, lon = row['lat_lon'].split('N')
                lon = lon.split('W')[0].strip()
                cur.execute(
                    "INSERT INTO Samples (sample_id, latitude, longitude, collection_date) VALUES (%s, %s, %s, %s) ON CONFLICT (sample_id) DO NOTHING;",
                    (row['NCBI_SRA_Accession'], float(lat.strip()), float(lon), row['Collection_Date'])
                )
            except (ValueError, AttributeError):
                print(f"Skipping sample {row['NCBI_SRA_Accession']} due to malformed lat_lon data.")
    conn.commit()
    print("'Samples' table populated.")

def populate_taxa_and_abundance(conn, marker, annotated_clusters_df, seq_table_df, metadata_df):
    print(f"Populating 'Taxa' and 'Abundance' tables for {marker}...")
    asv_to_taxon_id = {}
    with conn.cursor() as cur:
        unique_clusters = annotated_clusters_df.drop_duplicates(subset=['Cluster_ID'])
        for index, row in unique_clusters.iterrows():
            cluster_id = int(row['Cluster_ID'])
            if cluster_id == -1: continue # Skip noise
            cur.execute(
                "INSERT INTO Taxa (cluster_id, marker_type, annotation_name, confidence_level) VALUES (%s, %s, %s, %s) RETURNING taxon_id;",
                (cluster_id, marker, row['scientific_name'], row['confidence_level'])
            )
            taxon_id = cur.fetchone()[0]
            asvs_in_cluster = annotated_clusters_df[annotated_clusters_df['Cluster_ID'] == cluster_id]['ASV_ID']
            for asv_id in asvs_in_cluster:
                asv_to_taxon_id[asv_id] = taxon_id

        print(f"   - Processing abundance for {marker}...")
        valid_samples = set(metadata_df['NCBI_SRA_Accession'])
        for sample_id in seq_table_df.columns:
            if sample_id not in valid_samples: continue
            
            total_reads = seq_table_df[sample_id].sum()
            if total_reads == 0: continue
            
            taxon_counts = {}
            for asv_id, count in seq_table_df[sample_id].items():
                if count > 0 and asv_id in asv_to_taxon_id:
                    taxon_id = asv_to_taxon_id[asv_id]
                    taxon_counts[taxon_id] = taxon_counts.get(taxon_id, 0) + count
            
            for taxon_id, agg_count in taxon_counts.items():
                rel_abund = agg_count / total_reads
                cur.execute(
                    "INSERT INTO Abundance (sample_id, taxon_id, relative_abundance) VALUES (%s, %s, %s);",
                    (sample_id, taxon_id, rel_abund)
                )
    conn.commit()
    print(f"'{marker}' data populated.")

def main():
    # --- Check for the database URL ---
    if not DATABASE_URL:
        print("ERROR: DATABASE_URL environment variable not set.")
        print("Please set it before running the script, e.g.:")
        print("export DATABASE_URL='postgresql://user:pass@host/db?sslmode=require'")
        sys.exit(1)

    try:
        # Use the connection string from the environment variable
        conn = psycopg2.connect(DATABASE_URL, connect_timeout=15)
    except psycopg2.OperationalError as e:
        print(f"ERROR: Could not connect to the database. Please check your DATABASE_URL. Details: {e}")
        return
        
    with conn.cursor() as cur:
        # Clear all tables to ensure a fresh start
        cur.execute("TRUNCATE TABLE Abundance, Taxa, Samples CASCADE;")
    conn.commit()
    
    metadata = pd.read_csv(METADATA_PATH)
    # Filter metadata to only include samples present in our sequence tables
    seq_table_18s_cols = pd.read_csv(SEQ_TABLE_18S_PATH, sep="\t", index_col=0, nrows=0).columns.tolist()
    seq_table_coi_cols = pd.read_csv(SEQ_TABLE_COI_PATH, sep="\t", index_col=0, nrows=0).columns.tolist()
    all_samples = set(seq_table_18s_cols + seq_table_coi_cols)
    metadata = metadata[metadata['NCBI_SRA_Accession'].isin(all_samples)]
    metadata = metadata[metadata['lat_lon'].str.contains('N', na=False)]

    populate_samples(conn, metadata)
    
    annotated_18s = pd.read_csv(ANNOTATED_CLUSTERS_18S_PATH)
    seq_table_18s = pd.read_csv(SEQ_TABLE_18S_PATH, sep="\t", index_col=0)
    populate_taxa_and_abundance(conn, "18S", annotated_18s, seq_table_18s, metadata)
    
    annotated_coi = pd.read_csv(ANNOTATED_CLUSTERS_COI_PATH)
    seq_table_coi = pd.read_csv(SEQ_TABLE_COI_PATH, sep="\t", index_col=0)
    populate_taxa_and_abundance(conn, "COI", annotated_coi, seq_table_coi, metadata)

    conn.close()
    print("\n--- SUCCESS: Database population complete. ---")

if __name__ == "__main__":
    main()


