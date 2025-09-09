# Load the DECIPHER package
library(DECIPHER)

# Define the path to YOUR SILVA fasta file
fasta_path <- "../databases/silva_132.18s.99_rep_set.dada2.fa.gz"

# Read the sequences
seqs <- readDNAStringSet(fasta_path)

# Create the training set (THIS WILL BE SLOW)
trainingSet <- LearnTaxa(seqs)

# Save the formatted training set to an .RData file
save(trainingSet, file="../databases/SILVA_18S_IdTaxa.RData")

cat("Successfully created the 18S training file at: ../databases/SILVA_18S_IdTaxa.RData\n")