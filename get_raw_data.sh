mkdir -p 01_raw_data
cd 01_raw_data
# Download raw sequencing data using SRA Toolkit

echo "Downloading raw sequencing data..."

fastq-dump --split-files --gzip --outdir fastq_files SRR123456