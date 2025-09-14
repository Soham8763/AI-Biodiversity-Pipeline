# ================================================================= #
# R Script for Full Downstream Analysis of DADA2 Output
# - Assumes all packages are installed via Conda.
# ================================================================= #

# --- 1. Load Libraries ---
cat("Loading required R packages...\n")
library(dada2)
library(phyloseq)
library(ggplot2)
cat("All packages are loaded.\n\n")

# --- 2. THE ANALYSIS FUNCTION ---
# This function contains the core logic to process one marker gene dataset.
run_analysis_for_marker <- function(marker_name, seqtab_path, metadata_path, taxa_db_path, output_dir) {
  
  cat("=====================================================\n")
  cat("--- Starting Analysis for Marker:", marker_name, "---\n")
  cat("=====================================================\n\n")
  
  # Create output directory if it doesn't exist
  if (!dir.exists(output_dir)) {
    dir.create(output_dir, recursive = TRUE)
  }
  
  # --- Load Data ---
  cat("Step 1: Loading DADA2 output and metadata...\n")
  seqtab <- read.table(seqtab_path, header=TRUE, row.names=1)
  metadata <- read.csv(metadata_path)
  
  # --- NEW: Robustly handle sample matching ---
  # Find samples that are present in both the ASV table and the metadata
  shared_samples <- intersect(colnames(seqtab), metadata$NCBI_SRA_Accession)
  
  if (length(shared_samples) == 0) {
    stop("Error: No samples in common between the ASV table and the metadata file.")
  }
  
  cat("   - Found", length(shared_samples), "samples in both the ASV table and metadata.\n")
  
  # Filter both tables to keep only the shared samples
  seqtab <- seqtab[, shared_samples]
  metadata <- metadata[metadata$NCBI_SRA_Accession %in% shared_samples, ]
  
  # Ensure metadata rows are in the same order as ASV table columns
  rownames(metadata) <- metadata$NCBI_SRA_Accession
  metadata <- metadata[colnames(seqtab), ]
  cat("Data loaded and subset successfully.\n\n")
  
  # --- Assign Taxonomy (with memory-safe chunking) ---
  cat("Step 2: Assigning taxonomy... (This may take several minutes)\n")
  seqs <- rownames(seqtab)
  taxa_chunks <- list()
  chunk_size <- 4000 # Process 4000 sequences at a time
  seq_chunks <- split(seqs, ceiling(seq_along(seqs) / chunk_size))

  cat("   - Assigning taxonomy in", length(seq_chunks), "chunks of up to", chunk_size, "sequences each.\n")
  
  for (i in 1:length(seq_chunks)) {
    cat("     - Processing chunk", i, "of", length(seq_chunks), "\n")
    taxa_chunks[[i]] <- assignTaxonomy(seq_chunks[[i]], taxa_db_path, multithread=TRUE)
  }

  # Combine the results from all chunks
  taxa <- do.call(rbind, taxa_chunks)
  cat("Taxonomy assignment complete.\n\n")
  
  # --- Create Phyloseq Object ---
  cat("Step 3: Creating phyloseq object...\n")
  ps <- phyloseq(otu_table(seqtab, taxa_are_rows=TRUE), 
                 sample_data(metadata), 
                 tax_table(as.matrix(taxa)))
  print(ps)
  cat("\n")
  
  # --- Generate and Save Plot ---
  cat("Step 4: Generating and saving taxonomic bar plot...\n")
  output_plot_path <- file.path(output_dir, paste0(marker_name, "_taxa_barplot.pdf"))
  
  taxa_barplot <- plot_bar(ps, fill = "Phylum") +
    geom_bar(aes(color=Phylum, fill=Phylum), stat="identity", position="stack") +
    labs(x = "Sample", y = "Relative Abundance", title = paste("Taxonomic Composition for", marker_name)) +
    theme_bw() +
    theme(axis.text.x = element_text(angle = 90, vjust = 0.5, hjust = 1, size = 8))
  
  ggsave(output_plot_path, plot = taxa_barplot, width = 16, height = 9, dpi=300)
  cat("   - Plot saved to:", output_plot_path, "\n\n")
  
  # --- Save Phyloseq Object ---
  cat("Step 5: Saving phyloseq object for future use...\n")
  output_ps_path <- file.path(output_dir, paste0(marker_name, "_phyloseq.rds"))
  saveRDS(ps, file = output_ps_path)
  cat("   - Phyloseq object saved to:", output_ps_path, "\n\n")
  
  cat("--- Analysis for", marker_name, "is complete. ---\n\n")
  
  return(ps)
}

# ================================================================= #
# --- 3. USER CONFIGURATION & EXECUTION ---
# ================================================================= #

# --- Set Common File Paths ---
metadata_path <- "../databases/USGS_eDNA_Deepsearch_metadata.csv"
analysis_output_dir <- "../04_analysis_output"

# --- Define Settings for 18S Analysis ---
seqtab_18s_path <- "../03_dada2_output_18s_A/seqtab.tsv"
taxa_db_18s_path <- "../databases/pr2_version_5.1.0_SSU_dada2.fasta.gz"

# --- Define Settings for COI Analysis ---
# seqtab_coi_path <- "../03_dada2_output_coi/seqtab.tsv"
# taxa_db_coi_path <- "../databases/MIDORI2_UNIQ_NUC_GB267_CO1_DADA2.fasta.gz"

# --- Run the Full Analysis ---
cat("Starting the full downstream analysis pipeline...\n\n")

# Process the 18S dataset
ps_18s <- run_analysis_for_marker(
  marker_name = "18S_A",
  seqtab_path = seqtab_18s_path,
  metadata_path = metadata_path,
  taxa_db_path = taxa_db_18s_path,
  output_dir = file.path(analysis_output_dir, "18S_A")
)

# Process the COI dataset
# ps_coi <- run_analysis_for_marker(
#   marker_name = "COI",
#   seqtab_path = seqtab_coi_path,
#   metadata_path = metadata_path,
#   taxa_db_path = taxa_db_coi_path,
#   output_dir = file.path(analysis_output_dir, "COI")
# )

cat("============================================\n")
cat("🎉 All analyses are complete! 🎉\n")
cat("============================================\n")