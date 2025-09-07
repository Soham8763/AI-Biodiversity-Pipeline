#!/usr/bin/env Rscript
# run_dada2_final.R

# --- 0. Dependencies ---
if (!requireNamespace("dada2", quietly = TRUE)) {
  stop("Package 'dada2' is needed. Please install it.", call. = FALSE)
}
library(dada2)

# --- 1. Get Arguments (Now 8 total) ---
args <- commandArgs(trailingOnly = TRUE)
if (length(args) != 8) {
  stop("Usage: Rscript run_dada2_final.R <raw_dir> <out_dir> <fwd_primer> <rev_primer> <threads> <srr_list> <truncF> <truncR>", call. = FALSE)
}
raw_path <- args[1]
output_path <- args[2]
FWD_PRIMER <- args[3]
REV_PRIMER <- args[4]
n_threads <- as.integer(args[5])
srr_list_file <- args[6]
truncF <- as.integer(args[7])
truncR <- as.integer(args[8])

# --- 2. Initial Setup ---
cat("Processing marker with FWD primer:", FWD_PRIMER, "\n")
filt_path <- file.path(output_path, "filtered_reads")
if (!dir.exists(filt_path)) { dir.create(filt_path, recursive = TRUE) }

# --- 3. Prepare File Paths based on SRR List ---
# Read the specific sample IDs for this run
sample_ids_to_process <- readLines(srr_list_file)
cat("Found", length(sample_ids_to_process), "samples to process from:", basename(srr_list_file), "\n")

# List all fastq files in the raw directory
all_fnFs <- sort(list.files(raw_path, pattern = "_1.fastq.gz", full.names = TRUE))
all_fnRs <- sort(list.files(raw_path, pattern = "_2.fastq.gz", full.names = TRUE))

# **NEW**: Filter the file lists to ONLY include the samples for this run
fnFs <- all_fnFs[sapply(strsplit(basename(all_fnFs), "_"), `[`, 1) %in% sample_ids_to_process]
fnRs <- all_fnRs[sapply(strsplit(basename(all_fnRs), "_"), `[`, 1) %in% sample_ids_to_process]

if(length(fnFs) == 0) stop("No matching FASTQ files found in raw directory for the SRRs in the list provided.")

sample.names <- sapply(strsplit(basename(fnFs), "_"), `[`, 1)

# --- 4. Filter and Trim ---
filtFs <- file.path(filt_path, basename(fnFs))
filtRs <- file.path(filt_path, basename(fnRs))

out <- filterAndTrim(fnFs, filtFs, fnRs, filtRs,
                     trimLeft = c(nchar(FWD_PRIMER), nchar(REV_PRIMER)),
                     truncLen = c(truncF, truncR), # <<<--- Values now passed from master script
                     maxN = 0,
                     maxEE = c(2, 2),
                     truncQ = 2,
                     rm.phix = TRUE,
                     compress = TRUE,
                     multithread = n_threads)

cat("Filtering summary:\n"); print(out)
if(all(out[, "reads.out"] == 0)) {
    stop("No reads survived filtering. Check your truncLen/maxEE parameters and primer sequences.", call. = FALSE)
}

# --- 5. DADA2 Main Workflow (Unchanged) ---
# ... (The rest of the DADA2 workflow: learnErrors, dada, mergePairs, etc.) ...
# This part of the script remains exactly the same as the previous version.

# Use the new filtered file paths from this point onward.
filtFs <- sort(list.files(filt_path, pattern = "_1.fastq.gz", full.names = TRUE))
filtRs <- sort(list.files(filt_path, pattern = "_2.fastq.gz", full.names = TRUE))
sample.names <- sapply(strsplit(basename(filtFs), "_"), `[`, 1)

cat("Learning error rates...\n"); errF <- learnErrors(filtFs, multithread = n_threads)
cat("Learning error rates...\n"); errR <- learnErrors(filtRs, multithread = n_threads)

cat("Dereplicating sequences...\n"); derepFs <- derepFastq(filtFs, verbose = TRUE)
cat("Dereplicating sequences...\n"); derepRs <- derepFastq(filtRs, verbose = TRUE)
names(derepFs) <- sample.names; names(derepRs) <- sample.names

cat("Running DADA to infer ASVs...\n"); dadaFs <- dada(derepFs, err = errF, multithread = n_threads)
cat("Running DADA to infer ASVs...\n"); dadaRs <- dada(derepRs, err = errR, multithread = n_threads)

cat("Merging paired reads...\n"); mergers <- mergePairs(dadaFs, derepFs, dadaRs, derepRs, maxMismatch = 1, verbose = TRUE)
cat("Constructing sequence table...\n"); seqtab <- makeSequenceTable(mergers)

cat("Removing chimeras...\n"); seqtab.nochim <- removeBimeraDenovo(seqtab, method = "consensus", multithread = n_threads, verbose = TRUE)
cat("Dimensions of final ASV table:", dim(seqtab.nochim), "\n")

# --- 6. Save Outputs (Unchanged) ---
write.table(t(seqtab.nochim), file.path(output_path, "seqtab.tsv"), sep = "\t", row.names = TRUE, col.names = NA, quote = FALSE)
uniquesToFasta <- function(uniques, fout) {
  headers <- paste0(">ASV", seq_along(uniques))
  fasta <- as.vector(rbind(headers, uniques))
  write(fasta, fout)
}
uniquesToFasta(colnames(seqtab.nochim), file.path(output_path, "asv_sequences.fasta"))
cat("DADA2 pipeline complete! 👍 Output files are in:", output_path, "\n")