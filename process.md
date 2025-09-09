**Technical Documentation: AI-Enhanced eDNA Biodiversity Pipeline**
-------------------------------------------------------------------

### 1\. Objective & Scope

**Objective:** To implement an end-to-end bioinformatics pipeline that processes raw, dual-marker (18S rRNA and COI) eDNA sequencing data into a validated, bias-corrected biodiversity assessment.

**Scope:** This guide covers the entire workflow from raw `.fastq.gz` files to a populated PostgreSQL database ready to be served by a backend API. The process includes sequence pre-processing, AI-driven taxonomic clustering, hybrid annotation of known and novel lineages, machine learning-based bias correction, and final data aggregation. The final output is a clean, queryable database containing sample metadata, taxonomic annotations, and accurate relative abundance data for both genetic markers.

* * * * *

<br>

### 2\. Prerequisites & Dependencies

#### **Hardware Requirements**

-   **Server:** A Linux-based server (Ubuntu 20.04+ recommended).

-   **CPU:** Minimum 16 cores.

-   **RAM:** Minimum 64 GB (128 GB+ recommended for large datasets).

-   **Storage:** Minimum 1 TB NVMe SSD for fast I/O.

-   **GPU:** An NVIDIA GPU with at least 16 GB of VRAM (e.g., V100, A100) is **essential** for the CNN training phase.

#### **Software Dependencies**

It's highly recommended to use **Mamba** (a faster implementation of Conda) for environment management.

1.  **Create the main environment:**

    Bash

    ```
    mamba create -n edna-ai-env -c conda-forge -c bioconda python=3.9 r-base=4.2 cutadapt=4.1 dada2=1.24.0 blast=2.13.0 tiara=1.0.3 epa-ng=0.3.8 pytorch=2.0 torchvision torchaudio pytorch-cuda=11.8 numpy pandas matplotlib scikit-learn jupyterlab hdbscan-with-pip xgboost=1.7.5

    ```

2.  **Activate the environment:**

    Bash

    ```
    mamba activate edna-ai-env

    ```

3.  **Backend Stack:**

    -   **Database:** PostgreSQL 14+

    -   **Backend Framework:** Java 17+, Spring Boot 3+

    -   **Build Tool:** Maven or Gradle

* * * * *

<br>

### 3\. Detailed Step-by-Step Execution

This pipeline is designed to be run in parallel for the **18S** and **COI** datasets. The following commands use `MARKER` as a placeholder variable (e.g., `18S` or `COI`).

#### **Phase 1: Sequence Pre-processing & ASV Generation 🧬**

**Objective:** Clean raw reads and generate a high-fidelity Amplicon Sequence Variant (ASV) table.

1.  **Standardize Directory Structure:**

    Bash

    ```
    mkdir -p eDNA_project/{01_raw_data,02_trimmed_reads,03_dada2_output,04_ai_clustering,05_annotation,06_final_outputs}
    # Place your raw .fastq.gz files in 01_raw_data

    ```

2.  **Primer Trimming & Quality Filtering:**

    -   **Action:** Use `cutadapt` to remove primer sequences. You **must** know your forward and reverse primer sequences.

    -   **Command Example (for a single sample):**

        Bash

        ```
        # Replace with your actual primer sequences
        FWD_PRIMER="GTGCCAGCMGCCGCGGTAA" # Example 18S V4 Fwd
        REV_PRIMER="GGACTACHVGGGTWTCTAAT" # Example 18S V4 Rev

        cutadapt -a ${FWD_PRIMER}...${REV_PRIMER}\
                 -A ${REV_PRIMER_RC}...${FWD_PRIMER_RC} \ # RC = Reverse Complement
                 -o 02_trimmed_reads/sample1_${MARKER}_R1.fastq.gz\
                 -p 02_trimmed_reads/sample1_${MARKER}_R2.fastq.gz\
                 01_raw_data/sample1_${MARKER}_R1.fastq.gz\
                 01_raw_data/sample1_${MARKER}_R2.fastq.gz

        ```

    -   **Verification:** Check that output files are created in `02_trimmed_reads` and are not empty.

3.  **ASV Resolution with DADA2:**

    -   **Action:** Use an R script to run the DADA2 workflow. This is a multi-step process within the script.

    -   **Conceptual R Script (`run_dada2.R`):**

        R

        ```
        library(dada2)
        path <- "02_trimmed_reads"

        # 1. Filter and trim
        filtFs <- file.path(path, "filtered", paste0(sample.names, "_F_filt.fastq.gz"))
        filtRs <- file.path(path, "filtered", paste0(sample.names, "_R_filt.fastq.gz"))
        out <- filterAndTrim(fnFs, filtFs, fnRs, filtRs, truncLen=c(240,160), maxN=0, maxEE=c(2,2), truncQ=2, rm.phix=TRUE)

        # 2. Learn error rates
        errF <- learnErrors(filtFs, multithread=TRUE)
        errR <- learnErrors(filtRs, multithread=TRUE)

        # 3. Dereplicate, infer ASVs, merge pairs, and create sequence table
        dadaFs <- dada(derepFs, err=errF, multithread=TRUE)
        dadaRs <- dada(derepRs, err=errR, multithread=TRUE)
        mergers <- mergePairs(dadaFs, derepFs, dadaRs, derepRs, verbose=TRUE)
        seqtab <- makeSequenceTable(mergers)

        # 4. Remove chimeras
        seqtab.nochim <- removeBimeraDenovo(seqtab, method="consensus", multithread=TRUE, verbose=TRUE)

        # 5. Save outputs
        write.table(t(seqtab.nochim), "03_dada2_output/seqtab_${MARKER}.tsv", sep="\t", quote=F, col.names=NA)
        uniquesToFasta(seqtab.nochim, "03_dada2_output/asv_sequences_${MARKER}.fasta")

        ```

    -   **Verification:** Confirm the creation of `seqtab_${MARKER}.tsv` and `asv_sequences_${MARKER}.fasta` in the output directory.

* * * * *

#### **Phase 2: Unsupervised Learning for Taxa-Agnostic Clustering 🧠**

**Objective:** Use a CNN to convert ASV sequences into meaningful numerical representations and cluster them.

1.  **Sequence-to-Image Conversion (FCGR):**

    -   **Action:** Convert each FASTA sequence into a 2D image-like array.

    -   **Python Snippet (`fcgr.py`):**

        Python

        ```
        import numpy as np
        from Bio import SeqIO

        def fcgr(sequence, k=8):
            # CGR logic to map k-mers to coordinates
            # ... returns a 2**k x 2**k numpy array
            pass

        # Main script
        sequences = {record.id: str(record.seq) for record in SeqIO.parse("03_dada2_output/asv_sequences_${MARKER}.fasta", "fasta")}
        for seq_id, seq in sequences.items():
            img_array = fcgr(seq)
            np.save(f"04_ai_clustering/images/{seq_id}.npy", img_array)

        ```

2.  **CNN Model Training:**

    -   **Action:** Train a CNN on the generated FCGR images to learn their features and generate latent space vectors.

    -   **Conceptual PyTorch Snippet (`train_cnn.py`):**

        Python

        ```
        import torch
        # Define a CNN architecture (e.g., a simple ResNet-style model)
        class DeLUCS_like_CNN(torch.nn.Module):
            # ... Layers: Conv2D, BatchNorm, ReLU, MaxPool, etc.
            # The final layer before the classifier outputs the latent vector.
            pass

        model = DeLUCS_like_CNN().to('cuda')
        # Standard training loop: load data, define optimizer, define loss function (e.g., CrossEntropyLoss for a self-supervised task)
        # ...
        # After training, save the model
        torch.save(model.state_dict(), "04_ai_clustering/cnn_model_${MARKER}.pth")

        ```

3.  **Latent Space Clustering:**

    -   **Action:** Use the trained CNN to get latent vectors for all ASVs, then cluster these vectors using HDBSCAN.

    -   **Python Snippet (`cluster.py`):**

        Python

        ```
        import hdbscan
        import pandas as pd

        # 1. Load trained model and generate latent vectors for all ASV images
        # ... latent_vectors is a numpy array where rows are ASVs and columns are latent dimensions

        # 2. Perform clustering
        clusterer = hdbscan.HDBSCAN(min_cluster_size=5, min_samples=1, gen_min_span_tree=True)
        cluster_labels = clusterer.fit_predict(latent_vectors) # -1 indicates noise

        # 3. Map ASVs to clusters
        asv_ids = # List of ASV IDs from FASTA
        cluster_map = pd.DataFrame({'ASV_ID': asv_ids, 'Cluster_ID': cluster_labels})
        cluster_map.to_csv("04_ai_clustering/cluster_map_${MARKER}.csv", index=False)

        ```

    -   **Trade-off (HDBSCAN vs. K-Means):** **HDBSCAN** is chosen because it's a density-based algorithm. It doesn't require you to specify the number of clusters beforehand and can identify noise points (unclustered ASVs), which is ideal for discovering an unknown number of novel taxa.

    -   **Verification:** The `cluster_map_${MARKER}.csv` file should be created, containing a cluster assignment for every ASV.

* * * * *

#### **Phase 3: Hybrid Annotation and Contextualization 🏷️**

**Objective:** Assign taxonomic names to known clusters and place novel clusters on the tree of life.

1.  **Rapid Eukaryotic Screening:**

    -   **Action:** Pre-screen all ASVs with `tiara` to filter out non-target sequences (e.g., prokaryotic contamination).

    -   **Command:**

        Bash

        ```
        tiara -i 03_dada2_output/asv_sequences_${MARKER}.fasta -o 05_annotation/tiara_classification_${MARKER}.txt

        ```

    -   **Verification:** Inspect the output file. You can now focus downstream analysis on sequences classified as "eukarya" or "organelle".

2.  **Taxonomic Annotation via BLAST:**

    -   **Action:** For each cluster from Step 2, select the most abundant ASV as its representative. BLAST this representative against a curated database.

    -   **Database Choice:**

        -   For **18S**: Use the SILVA or PR2 databases.

        -   For **COI**: Use the BOLD (Barcode of Life Data System) or NCBI NT databases.

    -   **Command:**

        Bash

        ```
        blastn -query 05_annotation/representative_seqs_${MARKER}.fasta\
               -db path/to/your/db\
               -out 05_annotation/blast_results_${MARKER}.txt\
               -evalue 1e-5\
               -max_target_seqs 5\
               -outfmt "6 qseqid sseqid pident length mismatch gapopen qstart qend sstart send evalue bitscore staxids sscinames"

        ```

    -   **Verification:** Parse the BLAST output. Clusters with high-identity matches (e.g., >97%) can be confidently annotated. Others are flagged as "Potentially Novel".

3.  **Phylogenetic Placement of Novel Lineages:**

    -   **Action:** Use `epa-ng` to place the representative sequences of novel clusters onto a reference tree.

    -   **Command:**

        Bash

        ```
        epa-ng --ref-msa ref_alignment.fasta --tree ref_tree.newick --query 05_annotation/novel_reps_${MARKER}.fasta -o 05_annotation/epa_result_${MARKER}

        ```

    -   **Verification:** The output will include a `.newick` file (`epa_result.newick`) with the query sequences inserted. This can be visualized with tools like iTOL or FigTree.

* * * * *

#### **Phase 4: Machine Learning-Based Bias Correction 📊**

**Objective:** Correct for PCR amplification bias using a mock community and a trained ML model.

1.  **Process Mock Community Data:**

    -   **Action:** Run your mock community samples (with known organism concentrations) through **Phases 1-3**. This will give you the "observed" counts for a "known truth".

2.  **Train Bias-Correction Model:**

    -   **Action:** Train an XGBoost model that learns the relationship between the true concentrations (your ground truth) and the observed ASV counts from the sequencer.

    -   **Conceptual Python Snippet (`train_bias_model.py`):**

        Python

        ```
        import xgboost as xgb
        import pandas as pd

        # Load mock community data
        true_abundance = pd.read_csv("mock_truth.csv") # Columns: ASV_ID, True_Proportion
        observed_counts = pd.read_csv("mock_observed_counts.csv") # From seqtab.tsv

        # Create features: GC content, sequence length, homopolymers, etc.
        X_train = create_features_for_asvs(observed_counts['ASV_ID'])
        y_train = true_abundance['True_Proportion'] / observed_counts['Observed_Count'] # The correction factor

        # Train model
        model = xgb.XGBRegressor(objective='reg:squarederror', n_estimators=100)
        model.fit(X_train, y_train)

        # Save model
        model.save_model("06_final_outputs/bias_correction_model_${MARKER}.json")

        ```

3.  **Apply Bias Correction to Project Data:**

    -   **Action:** Load the trained model and use it to predict the correction factors for your actual project ASVs.

    -   **Conceptual Python Snippet (`apply_correction.py`):**

        Python

        ```
        # Load project data and trained model
        project_seqtab = pd.read_csv("03_dada2_output/seqtab_${MARKER}.tsv", sep="\t")
        model = xgb.XGBRegressor()
        model.load_model("06_final_outputs/bias_correction_model_${MARKER}.json")

        # Create features for project ASVs
        X_project = create_features_for_asvs(project_seqtab.index)

        # Predict correction factors and apply them
        correction_factors = model.predict(X_project)
        corrected_seqtab = project_seqtab.multiply(correction_factors, axis=0)

        corrected_seqtab.to_csv("06_final_outputs/corrected_seqtab_${MARKER}.tsv", sep="\t")

        ```

    -   **Verification:** Compare the distribution of counts in the original and corrected tables. The correction should adjust abundances, often boosting the representation of GC-poor or GC-rich sequences that amplify less efficiently.

* * * * *

#### **Phase 5: Database Population for Dashboard Backend**

**Objective:** Aggregate all processed data and load it into a structured PostgreSQL database.

1.  **Define Database Schema:**

    -   **SQL `CREATE TABLE` Statements:**

        SQL

        ```
        CREATE TABLE Samples (
            sample_id VARCHAR(50) PRIMARY KEY,
            latitude DECIMAL(9, 6),
            longitude DECIMAL(9, 6),
            collection_date DATE
            -- other metadata
        );

        CREATE TABLE Taxa (
            taxon_id SERIAL PRIMARY KEY,
            cluster_id INT,
            marker_type VARCHAR(10), -- '18S' or 'COI'
            annotation_name VARCHAR(255),
            confidence_level VARCHAR(50), -- 'High', 'Potentially Novel'
            phylogenetic_placement TEXT
        );

        CREATE TABLE Abundance (
            sample_id VARCHAR(50) REFERENCES Samples(sample_id),
            taxon_id INT REFERENCES Taxa(taxon_id),
            relative_abundance REAL,
            PRIMARY KEY (sample_id, taxon_id)
        );

        ```

2.  **Aggregate and Load Data:**

    -   **Action:** Write a final script (e.g., Python with `psycopg2`) that reads all your final output files (`corrected_seqtab`, `annotated_clusters`, sample metadata) and populates the PostgreSQL tables. This script will:

        -   Aggregate corrected counts per cluster/taxon.

        -   Normalize counts to calculate relative abundance per sample.

        -   Insert all data into the appropriate tables.

    -   **Verification:** Connect to the database using `psql` or a GUI like DBeaver. Run `SELECT` queries to confirm that the data has been loaded correctly. For example: `SELECT * FROM Taxa WHERE marker_type = '18S' LIMIT 10;`.

* * * * *

<br>

### 4\. Troubleshooting & Pitfalls

| Symptom | Possible Cause | Solution |
| --- | --- | --- |
| **Very few reads survive DADA2 `filterAndTrim`** | Quality scores are too low; `truncLen` is cutting off reads entirely. | Inspect read quality plots (FastQC). Adjust `truncLen` and `maxEE` parameters. Ensure primers are correct. |
| **CNN model loss does not decrease (not converging)** | Learning rate is too high/low; model architecture is too simple/complex. | Tune hyperparameters (learning rate). Try a different optimizer (e.g., AdamW). Simplify or deepen the CNN. |
| **HDBSCAN clusters everything as noise (-1)** | `min_cluster_size` is too large; data has no discernible cluster structure. | Decrease `min_cluster_size`. Check the latent space vectors for variance---they might all be too similar. |
| **BLAST returns no significant hits for any cluster** | The dataset is composed almost entirely of novel organisms; incorrect database used. | This could be a genuine discovery! Use phylogenetic placement (EPA-ng) as the primary tool for these. Double-check that you're using the right database (e.g., SILVA for 18S). |

* * * * *

<br>

### 5\. Verification & Validation

The primary validation step uses the **mock community** data, where the ground truth is known.

1.  **Taxonomic Accuracy (F-measure):**

    -   **Action:** Compare the taxonomic assignments of your pipeline (for the mock community) against the known composition.

    -   **Metric:** The F-measure combines precision and recall. A high F-measure (close to 1.0) indicates your pipeline is accurately identifying the correct taxa without making false assignments.

    -   **Tool:** Use `scikit-learn.metrics.f1_score`.

2.  **Abundance Accuracy (RMSE):**

    -   **Action:** Compare the final, bias-corrected relative abundances from your pipeline against the true, pre-defined proportions in the mock community.

    -   **Metric:** Root Mean Square Error (RMSE). A low RMSE indicates the corrected abundance values are very close to the true values.

    -   **Tool:** Use `scikit-learn.metrics.mean_squared_error` and take the square root.

3.  **Benchmarking:**

    -   **Action:** Run the same mock community data through a standard pipeline (e.g., QIIME2). Compare the F-measure and RMSE of your AI pipeline against the traditional one. This provides a quantitative measure of your pipeline's superior performance.

* * * * *

<br>

### 6\. Final Consolidated Workflow

Here is a high-level checklist to execute the entire process from start to finish.

**For EACH marker (18S and COI):**

-   [ ] **1\. Pre-process:**

    -   [ ] Set up directory structure.

    -   [ ] Run `cutadapt` to trim primers from all raw FASTQ files.

    -   [ ] Execute DADA2 R script to generate ASV table and sequences.

-   [ ] **2\. AI Clustering:**

    -   [ ] Convert ASV sequences to FCGR images.

    -   [ ] Train the CNN model on the images.

    -   [ ] Generate latent space vectors for all ASVs.

    -   [ ] Run HDBSCAN to get cluster assignments.

-   [ ] **3\. Annotation:**

    -   [ ] Run `tiara` for eukaryotic screening.

    -   [ ] Select representative sequences for each cluster.

    -   [ ] Run `blastn` against the appropriate database (SILVA/BOLD).

    -   [ ] Run `epa-ng` for novel lineages.

-   [ ] **4\. Bias Correction (using mock community model):**

    -   [ ] Train XGBoost model on mock community results (one-time prerequisite).

    -   [ ] Apply the trained model to the project's ASV table to get corrected counts.

**Final Integration:**

-   [ ] **5\. Database Population:**

    -   [ ] Run the final aggregation and loading script to populate the PostgreSQL database with results from both 18S and COI pipelines, ensuring the `marker_type` is specified.

-   [ ] **6\. Validation:**

    -   [ ] Calculate F-measure and RMSE using mock community results to validate pipeline performance.

    -   [ ] Document results and compare against a baseline pipeline.