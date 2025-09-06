#!/bin/bash

# Exit immediately if a command fails.
set -e

# --- Configuration ---
# The file containing the list of SRR accessions to process
SRR_LIST_FILE="srr_list.txt"

# The directories for input and output
RAW_DIR="01_raw_data"
TRIMMED_DIR="02_trimmed_reads"
TEMP_DIR="temp_trimmed"

# Primer sequences
FWD_PRIMER="GTCGGTTAAAACTCGTGCCAGC"
REV_PRIMER="CATAGTGGGGTATCTAATCCCAGTTTG"

# --- Script Logic ---

echo "Starting primer trimming and quality filtering for all samples in '$SRR_LIST_FILE'..."

# Create the output directory if it doesn't exist
mkdir -p "$TRIMMED_DIR"
mkdir -p "$TEMP_DIR"

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
             --minimum-length 25 --max-n 1 -q 20,20 \
             "$INPUT_R1" "$INPUT_R2"

    # --- Step 2: NEW - Filter for Valid DNA Characters ---
    # This step uses awk to check that every character in the sequence line (line 2 of every 4)
    # is one of A, C, G, or T. It keeps read pairs in sync.
    echo "[2/2] Filtering for valid ACGT characters..."
    FINAL_OUTPUT_R1="${TRIMMED_DIR}/${sra_id}_1.fastq.gz"
    FINAL_OUTPUT_R2="${TRIMMED_DIR}/${sra_id}_2.fastq.gz"
    
    paste <(zcat "$TEMP_OUTPUT_R1") <(zcat "$TEMP_OUTPUT_R2") | \
    awk 'BEGIN {FS="\t"; OFS="\t"} {
        # Store the 4 lines for a read pair
        lines[NR % 4, 1] = $1;
        lines[NR % 4, 2] = $2;
        # When we have a complete read (at the sequence quality line)
        if (NR % 4 == 0) {
            # Check if both sequence lines (line 2, which is index 2) contain only ACGT
            if (lines[2, 1] ~ /^[ACGT]+$/ && lines[2, 2] ~ /^[ACGT]+$/) {
                # If valid, print all 4 lines for the pair
                print lines[1, 1]"\n"lines[2, 1]"\n"lines[3, 1]"\n"lines[0, 1];
                print lines[1, 2]"\n"lines[2, 2]"\n"lines[3, 2]"\n"lines[0, 2];
            }
        }
    }' | \
    awk 'NR % 4 != 0 {printf "%s\t", $0} NR % 4 == 0 {print $0}' | \
    tee >(cut -f1 | gzip > "$FINAL_OUTPUT_R1") >(cut -f2 | gzip > "$FINAL_OUTPUT_R2") > /dev/null

    ((CURRENT_SAMPLE++))
done < "$SRR_LIST_FILE"

# Clean up the temporary directory
rm -r "$TEMP_DIR"

echo "Trimming complete! All cleaned files are in the '$TRIMMED_DIR' directory."
