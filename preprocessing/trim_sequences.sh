#!/bin/bash

# Exit immediately if a command fails.
set -e

# --- Configuration ---
# The file containing the list of SRR accessions to process
SRR_LIST_FILE="srr_list.txt"

# The directories for input and output
RAW_DIR="01_raw_data"
TRIMMED_DIR="02_trimmed_reads"

# Primer sequences
FWD_PRIMER="GTCGGTAAAACTCGTGCCAGC"
REV_PRIMER="CATAGTGGGGTATCTAATCCCAGTTTG"

# --- Script Logic ---

echo "Starting primer trimming and quality filtering for all samples in '$SRR_LIST_FILE'..."

# Create the output directory if it doesn't exist
mkdir -p "$TRIMMED_DIR"

# Check if the list file exists
if [ ! -f "$SRR_LIST_FILE" ]; then
    echo "Error: List file not found at '$SRR_LIST_FILE'"
    exit 1
fi

# Count the number of samples to process for user feedback
SAMPLE_COUNT=$(wc -l < "$SRR_LIST_FILE")
echo "Found $SAMPLE_COUNT samples to process."

# Read the file line by line and run cutadapt on each accession
CURRENT_SAMPLE=1
while IFS= read -r sra_id || [[ -n "$sra_id" ]]; do
    # Skip empty lines
    if [ -z "$sra_id" ]; then
        continue
    fi

    echo "Processing sample $CURRENT_SAMPLE of $SAMPLE_COUNT: ${sra_id}"

    # Define the input and output file paths for this specific sample
    INPUT_R1="${RAW_DIR}/${sra_id}_1.fastq.gz"
    INPUT_R2="${RAW_DIR}/${sra_id}_2.fastq.gz"
    OUTPUT_R1="${TRIMMED_DIR}/${sra_id}_1.fastq.gz"
    OUTPUT_R2="${TRIMMED_DIR}/${sra_id}_2.fastq.gz"

    # Run cutadapt with the improved parameters for better data retention
    cutadapt -a "$FWD_PRIMER" -A "$REV_PRIMER" \
             -o "$OUTPUT_R1" -p "$OUTPUT_R2" \
             --minimum-length 75 --max-n 1 -q 20,20 \
             "$INPUT_R1" "$INPUT_R2"

    ((CURRENT_SAMPLE++))
done < "$SRR_LIST_FILE"

echo "Trimming complete! All cleaned files are in the '$TRIMMED_DIR' directory."