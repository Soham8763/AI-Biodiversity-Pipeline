#!/bin/bash
set -e

# --- Global Configuration ---
DADA2_FINAL_SCRIPT="run_dada2.R" # Using the new, more powerful R script
RAW_DATA_DIR="../01_raw_data"
LOG_DIR="../logs"
TIMESTAMP=$(date +%Y-%m-%d_%H-%M-%S)
N_THREADS_PER_JOB=2
BIO_ENV="deepsea-pipeline"
DOWNLOAD_SCRIPT="get_raw_data.sh"

# --- Group 1: 18S Primer Set A Configuration ---
MARKER_18SA_NAME="18S_A"
SRR_LIST_18SA="srr_list_18s_A.txt"
DADA2_DIR_18SA="../03_dada2_output_18s_A"
FWD_PRIMER_18SA="GTACACACCGCCCGTC"
REV_PRIMER_18SA="TGATCCTTCTGCAGGTTCACCTAC"
TRUNCLEN_F_18SA=280 # Set based on FastQC for this marker
TRUNCLEN_R_18SA=250 # Set based on FastQC for this marker

# --- Group 2: 18S Primer Set B Configuration ---
MARKER_18SB_NAME="18S_B"
SRR_LIST_18SB="srr_list_18s_B.txt"
DADA2_DIR_18SB="../03_dada2_output_18s_B"
FWD_PRIMER_18SB="GGWACWGGWTGAACWGTWTAYCCYCC" 
REV_PRIMER_18SB="TAAACTTCAGGGTGACCAAAAAATCA" 
TRUNCLEN_F_18SB=280 # Run FastQC on these samples
TRUNCLEN_R_18SB=250 # Run FastQC on these samples

# --- Group 3: COI Configuration ---
MARKER_COI_NAME="COI"
SRR_LIST_COI="srr_list_coi.txt"
DADA2_DIR_COI="../03_dada2_output_coi"
FWD_PRIMER_COI="GGWACWGGWTGAACWGTWTAYCCYCC"
REV_PRIMER_COI="TAAACTTCAGGGTGACCAAAAAATCA"
TRUNCLEN_F_COI=280 # Run FastQC on these samples
TRUNCLEN_R_COI=220 # Run FastQC on these samples

# --- Pipeline Execution ---
mkdir -p "$LOG_DIR"
# (conda activation, permissions, etc. would go here)
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
#   echo "--- STARTING: $MARKER_18SA_NAME ---"
#   Rscript "$DADA2_FINAL_SCRIPT" \
#     "$RAW_DATA_DIR" \
#     "$DADA2_DIR_18SA" \
#     "$FWD_PRIMER_18SA" \
#     "$REV_PRIMER_18SA" \
#     "$N_THREADS_PER_JOB" \
#     "$SRR_LIST_18SA" \
#     "$TRUNCLEN_F_18SA" \
#     "$TRUNCLEN_R_18SA" 2>&1 | tee "${LOG_DIR}/${TIMESTAMP}_dada2_${MARKER_18SA_NAME}.log"
#   echo "--- FINISHED: $MARKER_18SA_NAME ---"
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

Rscript run_full_analysis.R 2>&1 | tee "${LOG_DIR}/${TIMESTAMP}_dada2_full_analysis.log"

echo "--- ✅ FULL PIPELINE COMPLETE FOR ALL MARKERS ---"