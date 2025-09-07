#!/bin/bash
set -e

# This is the master controller script.
# Define all marker-specific parameters here.

# --- Global Configuration ---
DOWNLOAD_SCRIPT="get_raw_data.sh"
TRIM_SCRIPT="./trim_sequences.sh"  # Note the ./
DADA2_SCRIPT="run_dada2.R"
LOG_DIR="logs"
BIO_ENV="deepsea-pipeline"
TIMESTAMP=$(date +%Y-%m-%d_%H-%M-%S)
N_THREADS_PER_JOB=4 # as i can give 12 logical cores so dividing among the 3

# --- Group 1: 18S Primer Set A Configuration ---
MARKER_18SA_NAME="18S_A"
SRR_LIST_18SA="srr_list_18s_A.txt"
TRIM_DIR_18SA="02_trimmed_reads_18s_A"
DADA2_DIR_18SA="03_dada2_output_18s_A"
# From your file:
FWD_PRIMER_18SA="GTACACACCGCCCGTC"
REV_PRIMER_18SA="TGATCCTTCTGCAGGTTCACCTAC"

# --- Group 2: 18S Primer Set B Configuration ---
MARKER_18SB_NAME="18S_B"
SRR_LIST_18SB="srr_list_18s_B.txt"
TRIM_DIR_18SB="02_trimmed_reads_18s_B"
DADA2_DIR_18SB="03_dada2_output_18s_B"
# From your file:
FWD_PRIMER_18SB="GGWACWGGWTGAACWGTWTAYCCYCC"
REV_PRIMER_18SB="TAAACTTCAGGGTGACCAAAAAATCA"


# --- Marker 2: COI Configuration ---
MARKER_COI_NAME="COI"
SRR_LIST_COI="srr_list_coi.txt"
TRIM_DIR_COI="02_trimmed_reads_coi"
DADA2_DIR_COI="03_dada2_output_coi"
# IMPORTANT: Replace with your actual COI primers (e.g., Leray primers)
FWD_PRIMER_COI="GGWACWGGWTGAACWGTWTAYCCYCC"
REV_PRIMER_COI="TAAACTTCAGGGTGACCAAAAAATCA"

# --- Pipeline Execution ---
mkdir -p "$LOG_DIR"

echo "--- Activating Conda Environment: $BIO_ENV ---"
eval "$(conda shell.bash hook)"
conda activate "$BIO_ENV"
echo "Environment activated."
echo ""

echo "--- STEP 0: SETTING SCRIPT PERMISSIONS ---"
chmod +x "$DOWNLOAD_SCRIPT"
chmod +x "$TRIM_SCRIPT"
echo "Permissions set."
echo ""

echo "--- STEP 1: RUNNING DOWNLOAD SCRIPT ---"
cat srr_list_18s_A.txt srr_list_18s_B.txt srr_list_coi.txt > srr_list_all.txt
./"$DOWNLOAD_SCRIPT" srr_list_all.txt 2>&1 | tee "${LOG_DIR}/${TIMESTAMP}_01_download.log"
echo "Download script finished."
echo ""

echo "--- STEP 2 & 3: LAUNCHING PARALLEL PROCESSING FOR ALL MARKER GROUPS ---"


# --- Launch Pipeline for 18S Primer Set A in the background ---
(
echo "--- STARTING PIPELINE FOR $MARKER_18SA_NAME ---"
echo "Step 2.1: Trimming $MARKER_18SA_NAME..."
$TRIM_SCRIPT "$SRR_LIST_18SA" "$TRIM_DIR_18SA" "$FWD_PRIMER_18SA" "$REV_PRIMER_18SA" 2>&1 | tee "${LOG_DIR}/${TIMESTAMP}_02_trimming_${MARKER_18SA_NAME}.log"
echo "Trimming for $MARKER_18SA_NAME finished."

echo "Step 3.1: Running DADA2 for $MARKER_18SA_NAME..."
Rscript "$DADA2_SCRIPT" "$TRIM_DIR_18SA" "$DADA2_DIR_18SA" "$N_THREADS_PER_JOB" 2>&1 | tee "${LOG_DIR}/${TIMESTAMP}_03_dada2_${MARKER_18SA_NAME}.log"
echo "DADA2 for $MARKER_18SA_NAME finished."
echo ""
) &

# --- Launch Pipeline for 18S Primer Set B in the background ---
(
echo "--- STARTING PIPELINE FOR $MARKER_18SB_NAME ---"
echo "Step 2.2: Trimming $MARKER_18SB_NAME..."
$TRIM_SCRIPT "$SRR_LIST_18SB" "$TRIM_DIR_18SB" "$FWD_PRIMER_18SB" "$REV_PRIMER_18SB" 2>&1 | tee "${LOG_DIR}/${TIMESTAMP}_02_trimming_${MARKER_18SB_NAME}.log"
echo "Trimming for $MARKER_18SB_NAME finished."

echo "Step 3.2: Running DADA2 for $MARKER_18SB_NAME..."
Rscript "$DADA2_SCRIPT" "$TRIM_DIR_18SB" "$DADA2_DIR_18SB" "$N_THREADS_PER_JOB" 2>&1 | tee "${LOG_DIR}/${TIMESTAMP}_03_dada2_${MARKER_18SB_NAME}.log"
echo "DADA2 for $MARKER_18SB_NAME finished."
echo ""
) &

# --- Launch Pipeline for COI in the background ---
(
echo "--- STARTING PIPELINE FOR $MARKER_COI_NAME ---"
echo "Step 2.3: Trimming $MARKER_COI_NAME..."
$TRIM_SCRIPT "$SRR_LIST_COI" "$TRIM_DIR_COI" "$FWD_PRIMER_COI" "$REV_PRIMER_COI" 2>&1 | tee "${LOG_DIR}/${TIMESTAMP}_02_trimming_${MARKER_COI_NAME}.log"
echo "Trimming for $MARKER_COI_NAME finished."

echo "Step 3.3: Running DADA2 for $MARKER_COI_NAME..."
Rscript "$DADA2_SCRIPT" "$TRIM_DIR_COI" "$DADA2_DIR_COI" "$N_THREADS_PER_JOB" 2>&1 | tee "${LOG_DIR}/${TIMESTAMP}_03_dada2_${MARKER_COI_NAME}.log"
echo "DADA2 for $MARKER_COI_NAME finished."
echo ""
) &

# --- Wait for all background jobs to complete ---
echo "All jobs launched. Waiting for completion..."
wait
echo ""

echo "--- ✅ FULL PIPELINE COMPLETE FOR ALL MARKERS ---"
echo "All logs for this run (ID: ${TIMESTAMP}) have been saved in the '$LOG_DIR' directory."

# Clean up the combined list
rm srr_list_all.txt