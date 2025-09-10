# ================================================================= #
# --- Professional-Grade Phyloseq Analysis Script (Final) ---
# This script provides an in-depth analysis pipeline, including:
# 1. Rigorous filtering of non-target taxa.
# 2. Statistical testing of alpha diversity.
# 3. Beta diversity analysis with PERMANOVA.
# 4. A robust heatmap of the most abundant ASVs using log-transformed counts.
# 5. Indicator species analysis to identify key taxa.
# ================================================================= #

# --- 0. SETUP ---
# Ensure necessary packages are installed. Run in R console if needed:
# if (!require("BiocManager", quietly = TRUE)) install.packages("BiocManager")
# BiocManager::install("DESeq2")
# install.packages(c("phyloseq", "ggplot2", "vegan", "indicspecies", "pheatmap"))

# Load required libraries
library(phyloseq)
library(ggplot2)
library(vegan)
library(indicspecies)
library(pheatmap)
library(DESeq2)

cat("Libraries loaded successfully.\n")


# --- 1. CONFIGURATION ---
rds_file_path <- "../04_analysis_output/18S_A/18S_A_phyloseq.rds"
output_dir <- "../04_analysis_output/18S_A/analysis"
metadata_column_for_grouping <- "Environmental_local_scale"


# --- 2. DATA LOADING AND FILTERING ---
if (!dir.exists(output_dir)) dir.create(output_dir, recursive = TRUE)
ps <- readRDS(rds_file_path)
cat("Phyloseq object loaded. Initial ASV count:", ntaxa(ps), "\n")

# Filter out non-target and unassigned taxa for a clean dataset
ps_filt <- subset_taxa(
  ps,
  !is.na(Domain) & !Domain %in% c("Bacteria", "Archaea") &
  !grepl(":plas", Subdivision, fixed = TRUE) &
  !grepl("mitochondria", Family, fixed = TRUE)
)
cat("   - After filtering:", ntaxa(ps_filt), "eukaryotic ASVs retained\n\n")


# --- 3. ALPHA DIVERSITY + STATISTICAL TEST ---
cat("--- Analyzing alpha diversity with statistics... ---\n")
alpha_div <- estimate_richness(ps_filt, measures = c("Observed", "Shannon"))
alpha_div$group <- sample_data(ps_filt)[[metadata_column_for_grouping]]

# Perform Wilcoxon Rank Sum test to compare Shannon diversity
if (nlevels(as.factor(alpha_div$group)) == 2) {
    shannon_test <- pairwise.wilcox.test(alpha_div$Shannon, alpha_div$group, p.adjust.method = "BH")
    cat("   - Pairwise Wilcoxon test results for Shannon Diversity:\n")
    print(shannon_test)
}

# Create the plot
alpha_plot <- plot_richness(ps_filt, x = metadata_column_for_grouping, measures = c("Observed", "Shannon")) +
  geom_boxplot(outlier.shape = NA) +
  geom_jitter(width = 0.1) +
  labs(title = "Alpha Diversity", x = metadata_column_for_grouping) +
  theme_bw() +
  theme(axis.text.x = element_text(angle = 45, vjust = 1, hjust = 1))

alpha_plot_path <- file.path(output_dir, "2c_alpha_diversity_stats.pdf")
ggsave(alpha_plot_path, plot = alpha_plot, width = 10, height = 6)
cat("   - Alpha diversity plot saved.\n\n")


# --- 4. BETA DIVERSITY (PCoA + PERMANOVA) ---
cat("--- Analyzing beta diversity... ---\n")
ps_filt_rel <- transform_sample_counts(ps_filt, function(x) x / sum(x))
bray_dist <- phyloseq::distance(ps_filt_rel, method = "bray")
sample_df <- data.frame(sample_data(ps_filt_rel))
permanova_result <- adonis2(bray_dist ~ get(metadata_column_for_grouping), data = sample_df)
cat("   - PERMANOVA results (p-value for community separation):\n")
print(permanova_result)

ordination <- ordinate(ps_filt_rel, method = "PCoA", distance = "bray")
beta_plot <- plot_ordination(ps_filt_rel, ordination, color = metadata_column_for_grouping) +
  geom_point(size = 5, alpha = 0.8) +
  labs(title = "Beta Diversity (PCoA on Bray-Curtis)", color = metadata_column_for_grouping) +
  theme_bw() +
  stat_ellipse(type = "t", level = 0.95)

beta_plot_path <- file.path(output_dir, "3c_beta_diversity_pcoa.pdf")
ggsave(beta_plot_path, plot = beta_plot, width = 8, height = 7)
cat("   - Beta diversity plot saved.\n\n")


# --- 5. ABUNDANT ASV HEATMAP ---
cat("--- Generating heatmap of the 25 most abundant ASVs... ---\n")

# Get the top 25 most abundant ASVs
top25_asvs <- names(sort(taxa_sums(ps_filt), decreasing = TRUE)[1:25])
ps_top25 <- prune_taxa(top25_asvs, ps_filt)

# --- !!! CORRECTION IS HERE !!! ---
# Use log-transformed counts for robust clustering
log_transformed_counts <- log10(otu_table(ps_top25) + 1)

# Get taxonomy for labeling - handle NAs gracefully
tax_labels_df <- as.data.frame(tax_table(ps_top25))
heatmap_labels <- paste(
  ifelse(is.na(tax_labels_df$Division), "Unknown", tax_labels_df$Division),
  ifelse(is.na(tax_labels_df$Genus), "Unknown", tax_labels_df$Genus),
  sep = "_"
)
# --- !!! END OF CORRECTION !!! ---

heatmap_path <- file.path(output_dir, "4_top25_asv_heatmap.pdf")
pheatmap(log_transformed_counts,
         cluster_rows = TRUE,
         cluster_cols = TRUE,
         labels_row = heatmap_labels,
         main = "Top 25 Most Abundant ASVs (Log10-Transformed Counts)",
         filename = heatmap_path,
         width = 10,
         height = 8)
cat("   - Heatmap saved.\n\n")


# --- 6. INDICATOR SPECIES ANALYSIS ---
cat("--- Identifying indicator species for each environment... ---\n")
otu <- as.data.frame(t(otu_table(ps_filt)))
groups <- sample_data(ps_filt)[[metadata_column_for_grouping]]

# Run the indicator species analysis
indicator_analysis <- multipatt(otu, groups, func = "r.g", control = how(nperm = 999))
cat("   - Summary of indicator species analysis (p-value <= 0.05):\n")

# Get significant results and add taxonomy
sig_indicators <- indicator_analysis$sign[which(indicator_analysis$sign$p.value <= 0.05), ]
if(nrow(sig_indicators) > 0) {
    tax <- as.data.frame(tax_table(ps_filt))
    sig_indicators$ASV <- rownames(sig_indicators)
    sig_indicators_taxa <- merge(sig_indicators, tax, by.x = "ASV", by.y = 0)
    print(sig_indicators_taxa)
} else {
    cat("   - No significant indicator species found at p-value <= 0.05.\n")
}

cat("\n🎉 All advanced analyses complete! Check the output directory for your new PDF plots. 🎉\n")