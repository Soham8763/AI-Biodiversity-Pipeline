# run_dada2.R (Modified for dynamic paths)

# --- 1. Get Arguments from Command Line ---
args <- commandArgs(trailingOnly = TRUE)
if (length(args) != 2) {
  stop("Usage: Rscript run_dada2.R <input_dir> <output_dir>", call. = FALSE)
}
path <- args[1]        # e.g., "02_trimmed_reads_18s"
output_path <- args[2] # e.g., "03_dada2_output_18s"

# --- 2. Initial Setup ---
library(dada2)
cat("Reading trimmed files from:", path, "\n")
if (!dir.exists(output_path)) {
    dir.create(output_path)
}

# The rest of your script remains the same...
# --- 3. Prepare File Paths ---
fnFs <- sort(list.files(path, pattern = "_1.fastq.gz", full.names = TRUE))
fnRs <- sort(list.files(path, pattern = "_2.fastq.gz", full.names = TRUE))

# Check if files were found
if(length(fnFs) == 0 || length(fnRs) == 0) {
  stop("No FASTQ files found in the specified input directory: ", path)
}

sample.names <- sapply(strsplit(basename(fnFs), "_"), `[`, 1)

# --- 4. DADA2 Workflow ---
cat("Learning error rates...\n")
errF <- learnErrors(fnFs, nbases = 1e8, multithread = TRUE)
errR <- learnErrors(fnRs, nbases = 1e8, multithread = TRUE)

mergers <- vector("list", length(sample.names))
names(mergers) <- sample.names

cat("Processing each sample individually...\n")
for(i in seq_along(sample.names)) {
  sam <- sample.names[i]
  cat("Processing:", sam, " (", i, "/", length(sample.names), ")\n")
  
  derepF <- derepFastq(fnFs[[i]])
  dadaF <- dada(derepF, err = errF, multithread = TRUE)
  
  derepR <- derepFastq(fnRs[[i]])
  dadaR <- dada(derepR, err = errR, multithread = TRUE)
  
  merger <- mergePairs(dadaF, derepF, dadaR, derepR)
  mergers[[i]] <- merger
}

seqtab <- makeSequenceTable(mergers)
seqtab.nochim <- removeBimeraDenovo(seqtab, method = "consensus", multithread = TRUE, verbose = TRUE)
cat("Dimensions of final ASV table:", dim(seqtab.nochim), "\n")

# --- 5. Save the final output files ---
write.table(t(seqtab.nochim), file.path(output_path, "seqtab.tsv"), sep = "\t", row.names = TRUE, col.names = NA, quote = FALSE)

uniquesToFasta <- function(uniques, fout) {
  headers <- paste0(">ASV", seq(from = 1, to = length(uniques)))
  fasta <- as.vector(rbind(headers, uniques))
  write(fasta, fout)
}
uniquesToFasta(colnames(seqtab.nochim), file.path(output_path, "asv_sequences.fasta"))

cat("DADA2 pipeline complete for marker. Output files are in '", output_path, "' directory.\n")