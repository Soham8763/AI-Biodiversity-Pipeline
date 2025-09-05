# run_dada2.R

# --- 1. Initial Setup ---
# Load the library
library(dada2)

# Define path to our trimmed reads
path <- "02_trimmed_reads"
cat("Reading trimmed files from:", path, "\n")

# Create the output directory as specified in the project plan
output_path <- "03_dada2_output"
if (!dir.exists(output_path)) {
    dir.create(output_path)
}

# --- 2. Prepare File Paths ---
# Get forward and reverse fastq file names
fnFs <- sort(list.files(path, pattern = "_1.fastq.gz", full.names = TRUE))
fnRs <- sort(list.files(path, pattern = "_2.fastq.gz", full.names = TRUE))

# Extract sample names
sample.names <- sapply(strsplit(basename(fnFs), "_"), `[`, 1)
cat("Processing sample names:", sample.names, "\n")

# --- 3. DADA2 Workflow ---
filtFs <- file.path(path, paste0(sample.names, "_1.fastq.gz"))
filtRs <- file.path(path, paste0(sample.names, "_2.fastq.gz"))
names(filtFs) <- sample.names
names(filtRs) <- sample.names

# Learn the error rates
cat("Learning error rates for forward reads...\n")
errF <- learnErrors(filtFs, multithread = TRUE)
cat("Learning error rates for reverse reads...\n")
errR <- learnErrors(filtRs, multithread = TRUE)

# Dereplicate sequences
cat("Dereplicating forward reads...\n")
derepFs <- derepFastq(filtFs, verbose = TRUE)
cat("Dereplicating reverse reads...\n")
derepRs <- derepFastq(filtRs, verbose = TRUE)

# Sample Inference (the core DADA2 algorithm)
cat("Applying core DADA2 algorithm...\n")
dadaFs <- dada(derepFs, err = errF, multithread = TRUE)
dadaRs <- dada(derepRs, err = errR, multithread = TRUE)

# Merge paired reads
cat("Merging paired reads...\n")
mergers <- mergePairs(dadaFs, derepFs, dadaRs, derepRs, verbose = TRUE)

# Construct the sequence table (ASV table)
seqtab <- makeSequenceTable(mergers)
cat("Dimensions of initial ASV table:", dim(seqtab), "\n")

# Remove chimeras [cite: 31]
seqtab.nochim <- removeBimeraDenovo(seqtab, method = "consensus", multithread = TRUE, verbose = TRUE)
cat("Dimensions of final ASV table after chimera removal:", dim(seqtab.nochim), "\n")
cat("Percentage of non-chimeric reads:", sum(seqtab.nochim) / sum(seqtab) * 100, "%\n")

# --- 4. Save the final output files ---
# This matches the output specified in the project plan 

# 1. The ASV Feature Table (seqtab.tsv) 
write.table(t(seqtab.nochim), file.path(output_path, "seqtab.tsv"), sep = "\t", row.names = TRUE, col.names = NA, quote = FALSE)

# 2. The FASTA file of ASV sequences (asv_sequences.fasta) 
uniquesToFasta <- function(uniques, fout) {
  headers <- paste0(">ASV", seq(from = 1, to = length(uniques)))
  fasta <- as.vector(rbind(headers, uniques))
  write(fasta, fout)
}
uniquesToFasta(colnames(seqtab.nochim), file.path(output_path, "asv_sequences.fasta"))

cat("DADA2 pipeline complete. Output files are in the '", output_path, "' directory.\n")
