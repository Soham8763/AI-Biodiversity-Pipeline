#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e

echo "Starting primer trimming and quality filtering..."

# 1. Create the output directory if it doesn't exist
mkdir -p 02_trimmed_reads

# 2. Define primer sequences
FWD_PRIMER="GTCGGTAAAACTCGTGCCAGC"
REV_PRIMER="CATAGTGGGGTATCTAATCCCAGTTTG"

# 3. Run cutadapt on all sample pairs
echo "Processing Nova Scotia samples..."
cutadapt -a $FWD_PRIMER -A $REV_PRIMER -o 02_trimmed_reads/SRR14899739_1.fastq.gz -p 02_trimmed_reads/SRR14899739_2.fastq.gz --minimum-length 100 --max-n 0 -q 20,20 01_raw_data/SRR14899739_1.fastq.gz 01_raw_data/SRR14899739_2.fastq.gz
cutadapt -a $FWD_PRIMER -A $REV_PRIMER -o 02_trimmed_reads/SRR14899738_1.fastq.gz -p 02_trimmed_reads/SRR14899738_2.fastq.gz --minimum-length 100 --max-n 0 -q 20,20 01_raw_data/SRR14899738_1.fastq.gz 01_raw_data/SRR14899738_2.fastq.gz
cutadapt -a $FWD_PRIMER -A $REV_PRIMER -o 02_trimmed_reads/SRR14899737_1.fastq.gz -p 02_trimmed_reads/SRR14899737_2.fastq.gz --minimum-length 100 --max-n 0 -q 20,20 01_raw_data/SRR14899737_1.fastq.gz 01_raw_data/SRR14899737_2.fastq.gz

echo "Processing British Columbia samples..."
cutadapt -a $FWD_PRIMER -A $REV_PRIMER -o 02_trimmed_reads/SRR14899748_1.fastq.gz -p 02_trimmed_reads/SRR14899748_2.fastq.gz --minimum-length 100 --max-n 0 -q 20,20 01_raw_data/SRR14899748_1.fastq.gz 01_raw_data/SRR14899748_2.fastq.gz
cutadapt -a $FWD_PRIMER -A $REV_PRIMER -o 02_trimmed_reads/SRR14899747_1.fastq.gz -p 02_trimmed_reads/SRR14899747_2.fastq.gz --minimum-length 100 --max-n 0 -q 20,20 01_raw_data/SRR14899747_1.fastq.gz 01_raw_data/SRR14899747_2.fastq.gz
cutadapt -a $FWD_PRIMER -A $REV_PRIMER -o 02_trimmed_reads/SRR14899746_1.fastq.gz -p 02_trimmed_reads/SRR14899746_2.fastq.gz --minimum-length 100 --max-n 0 -q 20,20 01_raw_data/SRR14899746_1.fastq.gz 01_raw_data/SRR14899746_2.fastq.gz

echo "Trimming complete! Cleaned files are in the 02_trimmed_reads directory."
