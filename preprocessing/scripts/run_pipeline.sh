#!/bin/bash
set -e

# --- Global Configuration ---
DADA2_FINAL_SCRIPT="run_dada2.R"
RAW_DATA_DIR="../01_raw_data"
LOG_DIR="../logs"
TIMESTAMP=$(date +%Y-%m-%d_%H-%M-%S)
N_THREADS_PER_JOB=2
BIO_ENV="aquanova"
DOWNLOAD_SCRIPT="get_raw_data.sh"
DOWNSTREAM_ANALYSIS_SCRIPT="dada2_downstream_analysis.R"
PHYLOSEQ_ANALYSIS_SCRIPT="analyze_phyloseq.R"
AI_CLUSTERING_SCRIPT="run_ai_clustering.py"
FORMAT_FASTA_SCRIPT="format_fasta_for_blast.py"
HYBRID_ANNOTATION_SCRIPT="run_hybrid_annotation.py"

# --- Database Configuration ---
DB_DIR="../databases"
PR2_FASTA_PATH="${DB_DIR}/pr2_version_5.1.0_SSU_dada2.fasta.gz"
MIDORI_FASTA_PATH="${DB_DIR}/MIDORI2_UNIQ_NUC_GB267_CO1_DADA2.fasta.gz"
PR2_BLAST_DB_NAME="pr2_blast_db"
MIDORI_BLAST_DB_NAME="midori2_blast_db"
PR2_FASTA_PATH_CLEAN="${DB_DIR}/pr2_blast/pr2_blast_formatted.fasta"
MIDORI_FASTA_PATH_CLEAN="${DB_DIR}/midori2_blast/midori2_blast_formatted.fasta"

# --- Group 1: 18S Primer Set A Configuration ---
MARKER_18SA_NAME="18S_A"
SRR_LIST_18SA="srr_list_18s_A.txt"
DADA2_DIR_18SA="../03_dada2_output_18s_A"
FWD_PRIMER_18SA="GTACACACCGCCCGTC"
REV_PRIMER_18SA="TGATCCTTCTGCAGGTTCACCTAC"
TRUNCLEN_F_18SA=280 # Set based on FastQC for this marker
TRUNCLEN_R_18SA=250 # Set based on FastQC for this marker

# # --- Group 2: 18S Primer Set B Configuration ---
# MARKER_18SB_NAME="18S_B"
# SRR_LIST_18SB="srr_list_18s_B.txt"
# DADA2_DIR_18SB="../03_dada2_output_18s_B"
# FWD_PRIMER_18SB="GGWACWGGWTGAACWGTWTAYCCYCC" 
# REV_PRIMER_18SB="TAAACTTCAGGGTGACCAAAAAATCA" 
# TRUNCLEN_F_18SB=280 # Set based on FastQC for this marker
# TRUNCLEN_R_18SB=250 # Set based on FastQC for this marker

# # --- Group 3: COI Configuration ---
# MARKER_COI_NAME="COI"
# SRR_LIST_COI="srr_list_coi.txt"
# DADA2_DIR_COI="../03_dada2_output_coi"
# FWD_PRIMER_COI="GGWACWGGWTGAACWGTWTAYCCYCC"
# REV_PRIMER_COI="TAAACTTCAGGGTGACCAAAAAATCA"
# TRUNCLEN_F_COI=280 # Set based on FastQC for this marker
# TRUNCLEN_R_COI=220 # Set based on FastQC for this marker

mkdir -p "$LOG_DIR"
echo "--- LAUNCHING PARALLEL DADA2 WORKFLOWS ---"

echo "--- Activating Conda Environment: $BIO_ENV ---"
eval "$(conda shell.bash hook)"
conda activate "$BIO_ENV"
echo "Environment activated."
echo ""

echo "--- STEP 0: SETTING SCRIPT PERMISSIONS ---"
chmod +x "$DOWNLOAD_SCRIPT"
echo "Permissions set."
echo ""

# echo "--- STEP 1: RUNNING DOWNLOAD SCRIPT ---"
# cat srr_list_18s_A.txt srr_list_18s_B.txt srr_list_coi.txt > srr_list_all.txt
# ./"$DOWNLOAD_SCRIPT" srr_list_all.txt 2>&1 | tee "${LOG_DIR}/${TIMESTAMP}_01_download.log"
# echo "Download script finished."
# echo ""

# # Launch Pipeline for 18S Primer Set A
# (
  echo "--- STARTING: $MARKER_18SA_NAME ---"
  Rscript "$DADA2_FINAL_SCRIPT" \
    "$RAW_DATA_DIR" \
    "$DADA2_DIR_18SA" \
    "$FWD_PRIMER_18SA" \
    "$REV_PRIMER_18SA" \
    "$N_THREADS_PER_JOB" \
    "$SRR_LIST_18SA" \
    "$TRUNCLEN_F_18SA" \
    "$TRUNCLEN_R_18SA" 2>&1 | tee "${LOG_DIR}/${TIMESTAMP}_dada2_${MARKER_18SA_NAME}.log"
  echo "--- FINISHED: $MARKER_18SA_NAME ---"
# ) &

# # Launch Pipeline for 18S Primer Set B
# (
#   echo "--- STARTING: $MARKER_18SB_NAME ---"
#   Rscript "$DADA2_FINAL_SCRIPT" \
#     "$RAW_DATA_DIR" \
#     "$DADA2_DIR_18SB" \
#     "$FWD_PRIMER_18SB" \
#     "$REV_PRIMER_18SB" \
#     "$N_THREADS_PER_JOB" \
#     "$SRR_LIST_18SB" \
#     "$TRUNCLEN_F_18SB" \
#     "$TRUNCLEN_R_18SB" 2>&1 | tee "${LOG_DIR}/${TIMESTAMP}_dada2_${MARKER_18SB_NAME}.log"
#   echo "--- FINISHED: $MARKER_18SB_NAME ---"
# ) &

# # Launch Pipeline for COI
# (
#   echo "--- STARTING: $MARKER_COI_NAME ---"
#   Rscript "$DADA2_FINAL_SCRIPT" \
#     "$RAW_DATA_DIR" \
#     "$DADA2_DIR_COI" \
#     "$FWD_PRIMER_COI" \
#     "$REV_PRIMER_COI" \
#     "$N_THREADS_PER_JOB" \
#     "$SRR_LIST_COI" \
#     "$TRUNCLEN_F_COI" \
#     "$TRUNCLEN_R_COI" 2>&1 | tee "${LOG_DIR}/${TIMESTAMP}_dada2_${MARKER_COI_NAME}.log"
#   echo "--- FINISHED: $MARKER_COI_NAME ---"
# ) &

# echo "All jobs launched in parallel. Waiting for completion..."
# wait

echo "--- STEP 2: Starting downstream analysis (Taxonomy, Phyloseq, Plots)... ---"
# Rscript "$DOWNSTREAM_ANALYSIS_SCRIPT" 2>&1 | tee "${LOG_DIR}/${TIMESTAMP}_dada2_downstream_analysis.log"
echo "Downstream analysis script has finished."
echo ""

echo "--- STEP 2: Starting phyloseq analysis ---"
# Rscript "$PHYLOSEQ_ANALYSIS_SCRIPT" 2>&1 | tee "${LOG_DIR}/${TIMESTAMP}_phyloseq_analysis.log"
echo "Phyloseq analysis script has finished."
echo ""

echo "--- STEP 2: Starting Ai clustering ---"
python "$AI_CLUSTERING_SCRIPT" 2>&1 | tee "${LOG_DIR}/${TIMESTAMP}_ai_clustering.log"
echo "Ai clustering script has finished."
echo ""

echo "--- STEP 2: Formatting FASTA files for BLAST... ---"
# Format PR2 if the clean file doesn't exist
if [ ! -f "$PR2_FASTA_PATH_CLEAN" ]; then
    echo "Formatting PR2 database..."
    python "$FORMAT_FASTA_SCRIPT" PR2
    echo "PR2 formatting complete."
else
    echo "Formatted PR2 file already exists. Skipping."
fi

# Format MIDORI2 if the clean file doesn't exist
# if [ ! -f "$MIDORI_FASTA_PATH_CLEAN" ]; then
#     echo "Formatting MIDORI2 database..."
#     python "$FORMAT_FASTA_SCRIPT" MIDORI2
#     echo "MIDORI2 formatting complete."
# else
#     echo "Formatted MIDORI2 file already exists. Skipping."
# fi
# echo ""


# --- STEP 2: CREATE BLAST DATABASES (if they don't exist) ---
echo "--- STEP 2: Preparing BLAST databases... ---"
# Create 18S BLAST DB
if [ ! -f "${DB_DIR}/pr2_blast/${PR2_BLAST_DB_NAME}.nsq" ]; then
  echo "Building 18S BLAST database from ${PR2_FASTA_PATH_CLEAN}..."
  makeblastdb -in "$PR2_FASTA_PATH_CLEAN" -dbtype nucl -out "${DB_DIR}/pr2_blast/${PR2_BLAST_DB_NAME}"
  echo "18S BLAST database created."
else
  echo "18S BLAST database already exists. Skipping."
fi

# Create COI BLAST DB
# if [ ! -f "${DB_DIR}/midori2_blast/${MIDORI_BLAST_DB_NAME}.nsq" ]; then
#   echo "Building COI BLAST database from ${MIDORI_FASTA_PATH_CLEAN}..."
#   makeblastdb -in "$MIDORI_FASTA_PATH_CLEAN" -dbtype nucl -out "${DB_DIR}/midori2_blast/${MIDORI_BLAST_DB_NAME}"
#   echo "COI BLAST database created."
# else
#   echo "COI BLAST database already exists. Skipping."
# fi
# echo ""

# --- STEP 5: RUN HYBRID ANNOTATION ---
echo "--- STEP 5: Starting hybrid AI/BLAST annotation... ---"
# Run for 18S
echo "   - Annotating 18S..."
python "$HYBRID_ANNOTATION_SCRIPT" 18S 2>&1 | tee "${LOG_DIR}/${TIMESTAMP}_annotation_18S.log"


# Run for COI
# echo "   - Annotating COI..."
# python "$HYBRID_ANNOTATION_SCRIPT" COI 2>&1 | tee "${LOG_DIR}/${TIMESTAMP}_annotation_COI.log"
echo "Hybrid annotation script has finished."
echo ""

echo "Populating database"
echo ""
# python populate_database.py 2>&1 | tee "${LOG_DIR}/${TIMESTAMP}_update_db.log"
echo ""

echo "--- ✅ FULL PIPELINE COMPLETE FOR ALL MARKERS ---"