#!/bin/bash
set -e

# --- Configuration from Arguments ---
# $1: SRR list file (e.g., srr_list_18s.txt)
# $2: Output directory (e.g., 02_trimmed_reads_18s)
# $3: Forward Primer Sequence
# $4: Reverse Primer Sequence

SRR_LIST_FILE="$1"
TRIMMED_DIR="$2"
FWD_PRIMER="$3"
REV_PRIMER="$4"

# --- Static Configuration ---
RAW_DIR="../01_raw_data"

# --- Script Logic ---
echo "Starting trimming for marker with FWD primer: $FWD_PRIMER"

mkdir -p "$TRIMMED_DIR"

if [ ! -f "$SRR_LIST_FILE" ]; then
    echo "Error: List file not found at '$SRR_LIST_FILE'"
    exit 1
fi

SAMPLE_COUNT=$(wc -l < "$SRR_LIST_FILE")
CURRENT_SAMPLE=1
while IFS= read -r sra_id || [[ -n "$sra_id" ]]; do
    if [ -z "$sra_id" ]; then continue; fi

    echo "Processing sample $CURRENT_SAMPLE of $SAMPLE_COUNT: ${sra_id}"

    INPUT_R1="${RAW_DIR}/${sra_id}_1.fastq.gz"
    INPUT_R2="${RAW_DIR}/${sra_id}_2.fastq.gz"
    OUTPUT_R1="${TRIMMED_DIR}/${sra_id}_1.fastq.gz"
    OUTPUT_R2="${TRIMMED_DIR}/${sra_id}_2.fastq.gz"

    # Note: I removed the awk filtering section from your script as it had some syntax
    # issues (e.g., TEMP_OUTPUT_R1 was not defined). Cutadapt's -q and --max-n
    # handle most quality filtering needs effectively.
    cutadapt -a "$FWD_PRIMER" -A "$REV_PRIMER" \
             -o "$OUTPUT_R1" -p "$OUTPUT_R2" \
             --minimum-length 25 --max-n 1 -q 20,20 \
             "$INPUT_R1" "$INPUT_R2"

    ((CURRENT_SAMPLE++))
done < "$SRR_LIST_FILE"

echo "Trimming complete! Cleaned files are in '$TRIMMED_DIR'."