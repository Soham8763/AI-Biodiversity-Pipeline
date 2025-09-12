import pandas as pd
import numpy as np
from Bio import SeqIO
from Bio.Seq import Seq
from Bio.SeqRecord import SeqRecord
import subprocess
import os

# ================================================================= #
# --- CONFIGURATION ---
# ================================================================= #
# CHOOSE YOUR MARKER: '18S' or 'COI'
MARKER = 'COI' # <--- CHANGE THIS TO 'COI' FOR THE SECOND RUN

# --- Use the base names you gave to 'makeblastdb' in the previous step ---
BLAST_DB_NAME_18S = "pr2_blast_db" 
BLAST_DB_NAME_COI = "midori2_blast_db"
# ================================================================= #

def check_blast_version():
    """Checks for BLAST+ installation and prints the version."""
    print("--- Checking BLAST+ installation ---")
    try:
        # Using shell=True for robust path finding on different systems
        result = subprocess.run('blastn -version', check=True, capture_output=True, text=True, shell=True)
        version_line = result.stdout.strip().split('\n')[0]
        print(f"Successfully found BLAST+. Version: {version_line}")
        print("-" * 34)
    except (subprocess.CalledProcessError, FileNotFoundError):
        print("\nERROR: 'blastn' command not found or failed to execute.")
        print("Please ensure that BLAST+ is installed and that its 'bin' directory is in your system's PATH.")
        exit()

# --- Set Paths Based on Marker ---
if MARKER == '18S':
    SEQ_TABLE_PATH = "../03_dada2_output_18s_A/seqtab.tsv"
    FASTA_PATH = "../03_dada2_output_18s_A/asv_sequences.fasta"
    CLUSTER_MAP_PATH = "../05_ai_clustering/18S_A/cluster_map_18S_cnn.csv"
    OUTPUT_DIR = "../06_annotation/18S_A/"
    BLAST_DB_NAME = BLAST_DB_NAME_18S
elif MARKER == 'COI':
    SEQ_TABLE_PATH = "../03_dada2_output_coi/seqtab.tsv"
    FASTA_PATH = "../03_dada2_output_coi/asv_sequences.fasta"
    CLUSTER_MAP_PATH = "../05_ai_clustering/COI/cluster_map_COI_cnn.csv"
    OUTPUT_DIR = "../06_annotation/COI/"
    BLAST_DB_NAME = BLAST_DB_NAME_COI
else:
    raise ValueError("Marker must be '18S' or 'COI'")

print(f"--- Starting Hybrid Annotation for Marker: {MARKER} ---")
if not os.path.exists(OUTPUT_DIR): os.makedirs(OUTPUT_DIR)

# --- 0. Verify BLAST+ Installation ---
check_blast_version()

# --- 1. Load Data ---
print("Loading input files...")
try:
    seq_table = pd.read_csv(SEQ_TABLE_PATH, sep="\t", index_col=0)
    cluster_map = pd.read_csv(CLUSTER_MAP_PATH)
    sequences = {record.id: str(record.seq) for record in SeqIO.parse(FASTA_PATH, "fasta")}
except FileNotFoundError as e:
    print(f"ERROR: Input file not found. Please check your paths. Details: {e}")
    exit()

# --- 2. Find Representative ASV for Each Cluster ---
print("Finding the most abundant representative for each cluster...")
asv_abundance_by_seq = seq_table.sum(axis=1).to_dict()
asv_id_to_abundance = {asv_id: asv_abundance_by_seq.get(seq_str, 0) for asv_id, seq_str in sequences.items()}
cluster_map['abundance'] = cluster_map['ASV_ID'].map(asv_id_to_abundance)
representatives = cluster_map.loc[cluster_map.groupby('Cluster_ID')['abundance'].idxmax()]
representatives = representatives[representatives['Cluster_ID'] != -1]
print(f"Found {len(representatives)} representative ASVs for {len(representatives)} clusters.")

# --- 3. Create a FASTA file of Representative Sequences ---
rep_fasta_path = os.path.join(OUTPUT_DIR, f"representative_seqs_{MARKER}.fasta")
rep_records = [SeqRecord(Seq(sequences[row['ASV_ID']]), id=f"Cluster_{row['Cluster_ID']}", description="") for index, row in representatives.iterrows()]
SeqIO.write(rep_records, rep_fasta_path, "fasta")
print(f"Representative sequences saved to: {rep_fasta_path}")

# --- 4. Run BLASTn ---
script_dir = os.path.dirname(os.path.realpath(__file__))
absolute_db_path = os.path.abspath(os.path.join(script_dir, "../databases/midori2_blast", BLAST_DB_NAME))

print(f"Verifying BLAST database exists at: {absolute_db_path}.nsq")
if not os.path.exists(f"{absolute_db_path}.nsq"):
    print("\nERROR: BLAST database index files not found at the specified path.")
    exit()

print(f"Running BLASTn against database: {absolute_db_path}...")
blast_output_path = os.path.join(OUTPUT_DIR, f"blast_results_{MARKER}.tsv")
blast_command = (
    f"blastn -query {rep_fasta_path} -db {absolute_db_path} -out {blast_output_path} "
    f"-evalue 1e-5 -max_target_seqs 1 -outfmt '6 qseqid stitle pident'"
)
try:
    subprocess.run(blast_command, shell=True, check=True, capture_output=True, text=True)
except subprocess.CalledProcessError as e:
    print("\n--- BLASTn Command Failed ---")
    print(f"ERROR: {e.stderr}")
    exit()
print(f"BLAST search complete. Results saved to: {blast_output_path}")

# --- 5. Create Final Annotated Cluster Table ---
print("Creating final annotated cluster table...")
try:
    blast_results = pd.read_csv(blast_output_path, sep="\t", header=None, names=['Cluster_ID_str', 'scientific_name', 'pident'])
    print("\nRaw BLAST results loaded:")
    print(blast_results.head())

    # --- !!! THIS IS THE CORRECTED SECTION !!! ---
    # Robustly parse the 'scientific_name' column from the BLAST 'stitle' output.
    # The previous method was flawed. This is the correct, robust way.
    def clean_stitle(title):
        if ' ' in title:
            return title.split(' ', 1)[1]
        return title
    
    blast_results['scientific_name'] = blast_results['scientific_name'].apply(clean_stitle)
    # --- !!! END OF CORRECTION !!! ---
    
    blast_results['Cluster_ID'] = blast_results['Cluster_ID_str'].str.replace('Cluster_', '').astype(int)
    blast_results['confidence_level'] = np.where(blast_results['pident'] >= 80.0, 'High', 'Potentially Novel')
    
    print("\nProcessed BLAST results before merging:")
    print(blast_results.head())

    final_table = pd.merge(cluster_map, blast_results[['Cluster_ID', 'scientific_name', 'confidence_level']], on='Cluster_ID', how='left')
except (pd.errors.EmptyDataError, FileNotFoundError):
    print("WARNING: BLAST search returned no hits. All clusters will be marked as unassigned.")
    final_table = cluster_map.copy()
    final_table['scientific_name'] = 'No Hit'
    final_table['confidence_level'] = 'Unknown'

final_table['confidence_level'] = final_table['confidence_level'].fillna('Noise')
final_table['scientific_name'] = final_table['scientific_name'].fillna('Unclustered')
final_output_path = os.path.join(OUTPUT_DIR, f"annotated_clusters_{MARKER}.csv")
final_table.to_csv(final_output_path, index=False)

print(f"\n--- SUCCESS: Final annotated table saved to {final_output_path} ---")

