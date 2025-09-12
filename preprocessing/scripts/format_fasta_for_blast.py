from Bio import SeqIO
import os
import gzip

# ================================================================= #
# --- CONFIGURATION ---
# This script reformats DADA2-style FASTA files to be compatible
# with makeblastdb by creating short, unique IDs.
# ================================================================= #

# --- CHOOSE WHICH DATABASE TO PROCESS ---
# Options: "PR2" or "MIDORI2"
DATABASE_TO_PROCESS = "PR2" # <--- EDIT THIS to "MIDORI2" for the second run
# ================================================================= #

# create directories if they don't already exist
os.makedirs("../databases/midori2_blast", exist_ok=True)
os.makedirs("../databases/pr2_blast", exist_ok=True)


# --- Script Logic ---
if DATABASE_TO_PROCESS == "PR2":
    INPUT_FASTA_PATH = "../databases/pr2_version_5.1.0_SSU_dada2.fasta.gz"
    OUTPUT_FASTA_PATH = "../databases/pr2_blast/pr2_blast_formatted.fasta"
    ID_PREFIX = "PR2"
elif DATABASE_TO_PROCESS == "MIDORI2":
    INPUT_FASTA_PATH = "../databases/MIDORI2_UNIQ_NUC_GB267_CO1_DADA2.fasta.gz"
    OUTPUT_FASTA_PATH = "../databases/midori2_blast/midori2_co1_blast_formatted.fasta"
    ID_PREFIX = "MIDORI2"
else:
    raise ValueError("DATABASE_TO_PROCESS must be 'PR2' or 'MIDORI2'")

print(f"--- Formatting {DATABASE_TO_PROCESS} database for BLAST ---")
print(f"Input: {INPUT_FASTA_PATH}")
print(f"Output: {OUTPUT_FASTA_PATH}")

# Use gzip.open for compressed .gz files
with gzip.open(INPUT_FASTA_PATH, "rt") as handle_in, open(OUTPUT_FASTA_PATH, "w") as handle_out:
    count = 0
    for record in SeqIO.parse(handle_in, "fasta"):
        count += 1
        original_id = record.id
        # Create a new short ID (e.g., "PR2_1")
        record.id = f"{ID_PREFIX}_{count}"
        # Move the original long taxonomy string to the description field
        record.description = original_id
        SeqIO.write(record, handle_out, "fasta")

print(f"\n--- SUCCESS: Processed {count} sequences. ---")
print(f"Your new BLAST-compatible file is ready at: {OUTPUT_FASTA_PATH}")