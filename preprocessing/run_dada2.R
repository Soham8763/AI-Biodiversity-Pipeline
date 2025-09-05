# run_dada2.R (Updated for large datasets)

# --- 1. Initial Setup ---
library(dada2)

path <- "02_trimmed_reads"
cat("Reading trimmed files from:", path, "\n")

output_path <- "03_dada2_output"
if (!dir.exists(output_path)) {
    dir.create(output_path)
}

# --- 2. Prepare File Paths ---
fnFs <- sort(list.files(path, pattern = "_1.fastq.gz", full.names = TRUE))
fnRs <- sort(list.files(path, pattern = "_2.fastq.gz", full.names = TRUE))

sample.names <- sapply(strsplit(basename(fnFs), "_"), `[`, 1)

# --- 3. DADA2 Workflow ---

# Learn Error Rates on a subset of data for speed
cat("Learning error rates on a subset of samples...\n")
errF <- learnErrors(fnFs, nbases = 1e8, multithread = TRUE)
errR <- learnErrors(fnRs, nbases = 1e8, multithread = TRUE)

# Create a list to hold the results for each sample
mergers <- vector("list", length(sample.names))
names(mergers) <- sample.names

# Process each sample individually in a loop
cat("Processing each sample individually (this will take time)...\n")
for(i in seq_along(sample.names)) {
  sam <- sample.names[i]
  cat("Processing:", sam, " (", i, "/", length(sample.names), ")\n")
  
  # Dereplicate and apply DADA2 algorithm
  derepF <- derepFastq(fnFs[[i]])
  dadaF <- dada(derepF, err = errF, multithread = TRUE)
  
  derepR <- derepFastq(fnRs[[i]])
  dadaR <- dada(derepR, err = errR, multithread = TRUE)
  
  # Merge the paired reads
  merger <- mergePairs(dadaF, derepF, dadaR, derepR)
  mergers[[i]] <- merger
}

# Construct the main ASV table from the list of merged results
seqtab <- makeSequenceTable(mergers)
cat("Dimensions of initial ASV table:", dim(seqtab), "\n")

# Remove chimeras
seqtab.nochim <- removeBimeraDenovo(seqtab, method = "consensus", multithread = TRUE, verbose = TRUE)
cat("Dimensions of final ASV table after chimera removal:", dim(seqtab.nochim), "\n")
cat("Percentage of non-chimeric reads:", sum(seqtab.nochim) / sum(seqtab) * 100, "%\n")

# --- 4. Save the final output files ---
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