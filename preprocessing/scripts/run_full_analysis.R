# ================================================================= #
# --- Bulletproof, Resumable R Script for DADA2 Analysis ---
#
# FEATURES:
# - Ultra-fine-grained logging with timestamps for every action.
# - Fully resumable workflow: automatically skips completed markers.
# - Checkpoint system: saves progress after every single chunk
#   and resumes from the last one after a crash or restart.
# ================================================================= #

# --- 1. Load Libraries ---
library(dada2)
library(phyloseq)
library(ggplot2)

# --- 2. HELPER FUNCTIONS ---

# Centralized logging function to add timestamps to all messages
log_message <- function(..., level = "INFO") {
  cat(paste0(format(Sys.time(), "[%Y-%m-%d %H:%M:%S]"), " [", level, "] ", ..., "\n"))
}

# Memory check function
check_memory <- function() {
  gc(verbose = FALSE)
  mem_info <- gc()
  log_message("Current memory usage: ", round(sum(mem_info[, 2]), 1), " MB")
}

# --- 3. THE ROBUST, RESUMABLE ANALYSIS FUNCTION ---
run_analysis_for_marker <- function(marker_name, seqtab_path, metadata_path, taxa_db_path, output_dir, tax_levels = NULL) {
  
  log_message("=====================================================")
  log_message("Starting analysis for marker: ", marker_name)
  log_message("=====================================================")
  
  # --- CHECKPOINT & RESUME LOGIC (FOR THE ENTIRE MARKER) ---
  output_ps_path <- file.path(output_dir, paste0(marker_name, "_phyloseq.rds"))
  if (file.exists(output_ps_path)) {
    log_message("SUCCESS: Final phyloseq object already exists. Skipping analysis for this marker.")
    log_message("   - Found at:", output_ps_path)
    return(readRDS(output_ps_path))
  }
  
  # Setup checkpoint directory for chunks
  checkpoint_dir <- file.path(output_dir, "checkpoints")
  if (!dir.exists(checkpoint_dir)) {
    dir.create(checkpoint_dir, recursive = TRUE)
    log_message("Created checkpoint directory at: ", checkpoint_dir)
  }
  
  # --- Step 1: Data Loading ---
  log_message("Step 1: Loading and preparing data...")
  check_memory()
  
  log_message("   - Reading ASV table: ", seqtab_path)
  seqtab <- read.table(seqtab_path, header=TRUE, row.names=1)
  log_message("   - ASV table loaded:", nrow(seqtab), "ASVs across", ncol(seqtab), "samples.")
  
  log_message("   - Reading metadata: ", metadata_path)
  metadata <- read.csv(metadata_path)
  log_message("   - Metadata loaded for", nrow(metadata), "samples.")

  log_message("   - Filtering and aligning samples between ASV table and metadata...")
  shared_samples <- intersect(colnames(seqtab), metadata$NCBI_SRA_Accession)
  if (length(shared_samples) == 0) stop("FATAL: No samples in common between ASV table and metadata.")
  seqtab <- seqtab[, shared_samples]
  metadata <- metadata[metadata$NCBI_SRA_Accession %in% shared_samples, ]
  seqtab <- seqtab[rowSums(seqtab) > 0, ]
  rownames(metadata) <- metadata$NCBI_SRA_Accession
  metadata <- metadata[colnames(seqtab), ]
  log_message("   - Data filtering complete. Retained", nrow(seqtab), "ASVs across", length(shared_samples), "samples.")
  check_memory()
  
  
  # --- Step 2: Resumable Taxonomy Assignment ---
  log_message("Step 2: Assigning taxonomy with checkpoint system...")
  seqs <- rownames(seqtab)
  total_seqs <- length(seqs)
  chunk_size <- 2000 # Using a fixed, safe chunk size for frequent checkpoints
  seq_chunks <- split(seqs, ceiling(seq_along(seqs) / chunk_size))
  log_message("   - To be processed:", total_seqs, "sequences in", length(seq_chunks), "chunks.")
  
  taxa_chunks <- list()
  
  for (i in 1:length(seq_chunks)) {
    checkpoint_file <- file.path(checkpoint_dir, paste0("taxa_chunk_", i, ".rds"))
    
    log_message("----------------------------------------------------")
    log_message("Checking for chunk ", i, " of ", length(seq_chunks), "...")
    
    if (file.exists(checkpoint_file)) {
      log_message("   - RESUMING: Checkpoint found. Loading result from disk.")
      taxa_chunks[[i]] <- readRDS(checkpoint_file)
    } else {
      log_message("   - PROCESSING: No checkpoint found. Starting new computation.")
      log_message("   - Contains", length(seq_chunks[[i]]), "sequences.")
      check_memory()
      
      current_chunk_taxa <- NULL
      log_message("   - Calling assignTaxonomy()... (This is the slow step, system may appear frozen)")
      
      # Time the slow step
      start_time <- Sys.time()
      if (!is.null(tax_levels)) {
        current_chunk_taxa <- assignTaxonomy(seq_chunks[[i]], taxa_db_path, taxLevels = tax_levels, multithread = TRUE, verbose = FALSE)
      } else {
        current_chunk_taxa <- assignTaxonomy(seq_chunks[[i]], taxa_db_path, multithread = TRUE, verbose = FALSE)
      }
      end_time <- Sys.time()
      
      log_message("   - assignTaxonomy() finished. Time elapsed:", round(end_time - start_time, 2), "seconds.")
      taxa_chunks[[i]] <- current_chunk_taxa
      
      log_message("   - SAVING CHECKPOINT for chunk ", i, " to: ", checkpoint_file)
      saveRDS(current_chunk_taxa, file = checkpoint_file)
      log_message("   - Checkpoint saved successfully.")
      check_memory()
    }
  }
  
  log_message("----------------------------------------------------")
  log_message("All chunks are now complete.")
  
  log_message("Step 3: Combining taxonomy results and creating phyloseq object...")
  taxa <- do.call(rbind, taxa_chunks)
  rm(taxa_chunks, seq_chunks) # Clean up memory
  gc()
  
  ps <- phyloseq(otu_table(as.matrix(seqtab), taxa_are_rows = TRUE), sample_data(data.frame(metadata)), tax_table(as.matrix(taxa)))
  log_message("   - Phyloseq object created successfully.")
  
  log_message("Step 4: Saving final phyloseq object...")
  saveRDS(ps, file = output_ps_path)
  log_message("   - Final object saved to:", output_ps_path)
  
  log_message("Analysis for marker ", marker_name, " is complete.")
  return(ps)
}

# --- 4. BATCH PROCESSING ---
run_sequential_analysis <- function() {
  log_message("Starting DADA2 downstream analysis pipeline...")
  
  # --- Set Common File Paths ---
  metadata_path <- "../databases/USGS_eDNA_Deepsearch_metadata.csv"
  analysis_output_dir <- "../04_analysis_output"
  
  # --- Process 18S Dataset ---
  # The script will automatically skip this if the final file already exists
  run_analysis_for_marker(
    marker_name = "18S_A",
    seqtab_path = "../03_dada2_output_18s_A/seqtab.tsv",
    metadata_path = metadata_path,
    taxa_db_path = "../databases/pr2_version_5.1.0_SSU_dada2.fasta.gz",
    output_dir = file.path(analysis_output_dir, "18S_A"),
    tax_levels = c("Domain","Supergroup","Division","Subdivision", "Class","Order","Family","Genus","Species")
  )
  gc() # Clean memory between markers
  
  # --- Process COI Dataset ---
  run_analysis_for_marker(
    marker_name = "COI",
    seqtab_path = "../03_dada2_output_coi/seqtab.tsv",
    metadata_path = metadata_path,
    taxa_db_path = "../databases/MIDORI2_UNIQ_NUC_GB267_CO1_DADA2.fasta.gz",
    output_dir = file.path(analysis_output_dir, "COI")
  )
  gc()
  
  log_message("============================================")
  log_message("🎉 ALL MARKERS PROCESSED SUCCESSFULLY! 🎉")
  log_message("============================================")
}

# --- 5. EXECUTION ---
run_sequential_analysis()