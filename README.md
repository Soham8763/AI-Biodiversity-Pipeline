AI-Driven Pipeline for Deep-Sea Biodiversity Discovery
======================================================

**Prepared for:** Centre for Marine Living Resources and Ecology (CMLRE)

1\. Introduction
----------------

This project provides an end-to-end, AI-driven pipeline to discover and analyze deep-sea biodiversity from environmental DNA (eDNA) datasets. The system translates raw DNA sequencing files into a fully interactive web-based dashboard, enabling researchers to explore taxonomic compositions, identify potentially novel lineages, and visualize complex ecological data.

The core of this pipeline uses a Variational Autoencoder (VAE) for taxa-agnostic clustering of Amplicon Sequence Variants (ASVs), allowing for the discovery of microbial groups without prior taxonomic knowledge.

2\. Development Status: 🚧 In Progress
--------------------------------------

> **Please Note:** This project is currently under active construction.
>
> -   **Data Pre-processing:** Complete.
>
>
> -   **AI Model (VAE):** Undergoing testing. Results and model files will be updated here soon.
>
>
> -   **Backend & Frontend:** Implementation has not yet started.
>
>
>
> Thank you for your interest! Check back for updates.

3\. The Pipeline Workflow
-------------------------

The project is divided into two main parts: a core data processing pipeline and an interactive discovery dashboard.

### Part 1: Core Data Processing & AI Analysis

1.  **Sequence Pre-processing & ASV Generation**: Raw FASTQ files are trimmed, filtered, and processed using **DADA2** to generate high-fidelity Amplicon Sequence Variants (ASVs).

2.  **Unsupervised Deep Learning**: A **Variational Autoencoder (VAE)** implemented in PyTorch/TensorFlow converts ASV sequences into numerical vectors. These vectors are then clustered using **HDBSCAN** to group similar ASVs into "putative taxa."

3.  **Hybrid Annotation & Phylogenetics**: Clusters are annotated using **BLAST+**. Clusters that fall below a certain identity threshold are flagged as "Potentially Novel Lineages" and placed onto a reference phylogenetic tree using **EPA-ng** to determine their evolutionary context.

4.  **Abundance Estimation**: The relative abundance of each final annotated taxon is calculated across all samples, creating a master data matrix for visualization.

### Part 2: Interactive Dashboard

1.  **Backend API**: A robust **Java / Spring Boot** RESTful API serves the processed data from a **PostgreSQL** database.

2.  **Frontend Application**: A responsive user interface built with **React (or Vue.js)** and **D3.js** allows for intuitive data exploration through interactive heatmaps, charts, and a dynamic phylogenetic tree viewer.

4\. Technology Stack
--------------------

| **Area** | **Technologies & Tools** | | **Data Processing** | `cutadapt`, `R (DADA2)`, `Python` | | **AI / ML** | `PyTorch` / `TensorFlow`, `HDBSCAN` | | **Bioinformatics** | `BLAST+`, `EPA-ng` | | **Backend** | `Java`, `Spring Boot`, `PostgreSQL` | | **Frontend** | `React` / `Vue.js`, `D3.js`, `JavaScript`, `HTML/CSS` |

5\. Project Structure
---------------------

The project uses a standardized folder structure to manage the data workflow:

```
/
├── 01_raw_data/              # Input .fastq.gz files
├── 02_trimmed_reads/         # Output from cutadapt
├── 03_dada2_output/          # ASV tables and sequences from DADA2
├── 04_ai_outputs/            # VAE model, latent vectors, cluster maps
├── 05_annotation_outputs/    # BLAST results, annotated clusters, tree files
├── 06_final_data/            # Final relative abundance matrix for the dashboard
├── backend/                  # Spring Boot API source code
├── frontend/                 # React/Vue.js application source code
└── scripts/                  # All shell, R, and Python scripts for the pipeline

```

6\. Getting Started
-------------------

### Prerequisites

-   Conda / Miniconda

-   R and required packages (e.g., dada2)

-   Java JDK

-   Node.js and npm/yarn

-   PostgreSQL server

### Installation & Usage

1.  **Clone the repository:**

    ```
    git clone [https://github.com/your-username/your-repo-name.git](https://github.com/your-username/your-repo-name.git)
    cd your-repo-name

    ```

2.  **Set up the bioinformatics environment** using the provided configuration files.

3.  **Place raw `.fastq.gz` files** into the `01_raw_data/` directory.

4.  **Execute the main pipeline script:**

    ```
    bash scripts/run_pipeline.sh

    ```

5.  **Set up and populate the database** using the scripts in the `backend/` directory.

6.  **Launch the Backend API.**

7.  **Launch the Frontend Application.**

8.  Open your browser to `http://localhost:3000` to view the dashboard.

7\. Contributing
----------------

Contributions are welcome! Please feel free to submit a pull request or open an issue.

8\. License
-----------

This project is licensed under the MIT License. See the `LICENSE` file for details.
