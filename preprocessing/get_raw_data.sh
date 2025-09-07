#!/bin/bash

# Exit immediately if a command fails
set -e

# --- Configuration ---
# The name of the file containing the list of SRR accessions
SRR_LIST_FILE="$1"

# The directory where raw data will be saved
OUTPUT_DIR="01_raw_data"

# --- Script Logic ---

# Check if the list file exists
if [ ! -f "$SRR_LIST_FILE" ]; then
    echo "Error: List file not found at '$SRR_LIST_FILE'"
    exit 1
fi

# Create the output directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"

echo "Starting download process for all samples in '$SRR_LIST_FILE'..."
echo "Existing files will be skipped."
echo ""

# Read the file line by line and download each accession
while IFS= read -r sra_id || [[ -n "$sra_id" ]]; do
    # Skip empty lines
    if [ -z "$sra_id" ]; then
        continue
    fi
    
    # Define the expected output file path
    EXPECTED_FILE="${OUTPUT_DIR}/${sra_id}_1.fastq.gz"
    
    # Check if the first FASTQ file already exists
    if [ -f "$EXPECTED_FILE" ]; then
        echo "File for ${sra_id} already exists. Skipping."
    else
        echo "Downloading ${sra_id}..."
        fastq-dump --split-files --gzip --outdir "$OUTPUT_DIR" "${sra_id}"
        echo "Downloaded ${sra_id} to ${OUTPUT_DIR}"
    fi
    echo ""

done < "$SRR_LIST_FILE"

echo "All downloads are complete."
