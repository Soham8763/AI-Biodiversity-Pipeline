#!/bin/bash

set -e

SRR_LIST_FILE="$1"

OUTPUT_DIR="../01_raw_data"

if [ ! -f "$SRR_LIST_FILE" ]; then
    echo "Error: List file not found at '$SRR_LIST_FILE'"
    exit 1
fi

mkdir -p "$OUTPUT_DIR"

echo "Starting download process for all samples in '$SRR_LIST_FILE'..."
echo "Existing files will be skipped."
echo ""

while IFS= read -r sra_id || [[ -n "$sra_id" ]]; do
    if [ -z "$sra_id" ]; then
        continue
    fi
    
    EXPECTED_FILE_1="${OUTPUT_DIR}/${sra_id}_1.fastq.gz"
    EXPECTED_FILE_2="${OUTPUT_DIR}/${sra_id}_2.fastq.gz"
    if [ -f "$EXPECTED_FILE_1" ] && [ -f "$EXPECTED_FILE_2" ]; then
    echo "Files for ${sra_id} already exist. Skipping."
    else
        echo "Downloading ${sra_id}..."
        fastq-dump --split-files --gzip --outdir "$OUTPUT_DIR" "${sra_id}"
        echo "Downloaded ${sra_id} to ${OUTPUT_DIR}"
    fi
    echo ""

done < "$SRR_LIST_FILE"

echo "All downloads are complete."
