#!/bin/bash

# This is a simple controller script to run the bioinformatics pipeline steps in order.
# It assumes you will activate the conda environment manually before running it.

# Exit immediately if any command fails.
set -e

# --- Configuration ---
# The names of your existing scripts
DOWNLOAD_SCRIPT="get_raw_data.sh"
TRIM_SCRIPT="trim_sequences.sh"
DADA2_SCRIPT="run_dada2.R"
BIO_ENV="deepsea-pipeline"

# --- Script Execution ---

echo "--- Activating Conda Environment: $BIO_ENV ---"
eval "$(conda shell.bash hook)"
conda activate "$BIO_ENV"
echo "Environment activated."
echo ""

echo "--- STEP 0: SETTING SCRIPT PERMISSIONS ---"
chmod +x "$DOWNLOAD_SCRIPT"
chmod +x "$TRIM_SCRIPT"
echo "Permissions set."
echo ""

echo "--- STEP 1: RUNNING DOWNLOAD SCRIPT ---"
./"$DOWNLOAD_SCRIPT"
echo "Download script finished."
echo ""

echo "--- STEP 2: RUNNING TRIMMING SCRIPT ---"
./"$TRIM_SCRIPT"
echo "Trimming script finished."
echo ""

echo "--- STEP 3: RUNNING DADA2 SCRIPT ---"
Rscript "$DADA2_SCRIPT"
echo "DADA2 script finished."
echo ""

echo "--- FULL PIPELINE COMPLETE ---"
```

### How to Use

1.  **Save this file** as `run_all.sh` in your main project directory.
2.  **Give this one script execute permissions** (you only have to do this once):
    ```bash
    chmod +x run_all.sh
    ```
3.  **Run it:**
    ```bash
    ./run_all.sh
    
