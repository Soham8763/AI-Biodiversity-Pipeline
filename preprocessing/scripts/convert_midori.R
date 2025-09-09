# Load the required R packages
library(DECIPHER)
library(Biostrings)

# Define the path to your MIDORI fasta file
fasta_path <- "../databases/MIDORI2_UNIQ_NUC_GB267_CO1_DADA2.fasta.gz"

# Read the sequences from the FASTA file
seqs <- readDNAStringSet(fasta_path)

# Manually extract the taxonomy string from the headers
tax_strings <- sapply(strsplit(names(seqs), " "), `[`, 2)

# --- NEW STEP: Add the required "Root;" prefix to each taxonomy string ---
tax_strings_rooted <- paste0("Root;", tax_strings)

# Create the training set using the newly prefixed taxonomy
trainingSet <- LearnTaxa(seqs, taxonomy = tax_strings_rooted) # <-- CORRECTED

# Save the formatted training set to an .RData file for future use
save(trainingSet, file="../databases/MIDORI2_COI_IdTaxa.RData")

cat("Successfully created the COI training file at: ../databases/MIDORI2_COI_IdTaxa.RData\n")