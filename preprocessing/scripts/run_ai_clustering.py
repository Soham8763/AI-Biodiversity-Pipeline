import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader, TensorDataset
import pandas as pd
import numpy as np
import hdbscan
from Bio import SeqIO
import os
import gc
import matplotlib.pyplot as plt # <-- ADDED FOR IMAGE SAVING

# ================================================================= #
# --- CONFIGURATION ---
# ================================================================= #
# CHOOSE YOUR MARKER: '18S' or 'COI'
MARKER = '18S' # <--- CHANGE THIS LINE TO 'COI' TO RUN THE OTHER DATASET
# ================================================================= #


# --- Global Settings (can be tuned) ---
KMER_SIZE = 7  # k=7 -> 128x128 images. Good balance of detail and memory usage.
LATENT_DIM = 32 # Size of the compressed numerical vector for each ASV.
EPOCHS = 20    # How many times to train on the full dataset.
BATCH_SIZE = 16 # Small batch size to fit on a 4GB GPU.


# --- Set Paths Based on Marker ---
if MARKER == '18S':
    FASTA_FILE = "../03_dada2_output_18s_A/asv_sequences.fasta"
    OUTPUT_DIR = "../05_ai_clustering/18S_A/"
elif MARKER == 'COI':
    # Assuming the COI DADA2 outputs are in a parallel directory
    FASTA_FILE = "../03_dada2_output_coi/asv_sequences.fasta"
    OUTPUT_DIR = "../05_ai_clustering/COI/"
else:
    raise ValueError("ERROR: MARKER in configuration must be either '18S' or 'COI'")

# --- !!! NEW: DEFINE PATH FOR SAVED IMAGES !!! ---
IMAGE_SAVE_DIR = os.path.join(OUTPUT_DIR, "fcgr_images")

print(f"--- Configuration set for Marker: {MARKER} ---")
print(f"Input FASTA: {FASTA_FILE}")
print(f"Output Directory: {OUTPUT_DIR}")


# --- 1. FCGR IMAGE GENERATION ---
print("\n--- Step 1: Generating FCGR Image Representations ---")
# Mapping nucleotides to coordinates for the Frequency Chaos Game Representation
CGR_COORDS = {'A': np.array([0, 0]), 'C': np.array([0, 1]),
              'G': np.array([1, 1]), 'T': np.array([1, 0]),
              'N': np.array([0.5, 0.5])} # Handle Ns

def fcgr(sequence, k):
    """Generates a Frequency Chaos Game Representation (FCGR) image array."""
    img_size = 2**k
    fcgr_array = np.zeros((img_size, img_size))
    last_coord = np.array([0.5, 0.5])

    for base in sequence.upper():
        if base not in CGR_COORDS: continue
        last_coord = (last_coord + CGR_COORDS[base]) / 2.0


        x_idx = min(int(last_coord[0] * img_size), img_size - 1)
        y_idx = min(int(last_coord[1] * img_size), img_size - 1)


        fcgr_array[x_idx, y_idx] += 1
    return fcgr_array

# Load sequences
asv_ids = [record.id for record in SeqIO.parse(FASTA_FILE, "fasta")]
sequences = [str(record.seq) for record in SeqIO.parse(FASTA_FILE, "fasta")]
print(f"Loaded {len(sequences)} ASV sequences.")

# Create FCGR images in memory
fcgr_images = np.array([fcgr(seq, KMER_SIZE) for seq in sequences])

if not os.path.exists(IMAGE_SAVE_DIR):
    os.makedirs(IMAGE_SAVE_DIR)
print(f"Saving {len(asv_ids)} FCGR images to: {IMAGE_SAVE_DIR}")
for i, asv_id in enumerate(asv_ids):
    plt.imsave(
        os.path.join(IMAGE_SAVE_DIR, f"{asv_id}.png"),
        fcgr_images[i],
        cmap='gray'
    )

fcgr_images = np.expand_dims(fcgr_images, axis=1)
print(f"Generated {fcgr_images.shape[0]} FCGR images of size {fcgr_images.shape[2]}x{fcgr_images.shape[3]}.")


# --- 2. DEFINE AND TRAIN CNN AUTOENCODER ---
print("\n--- Step 2: Training CNN Autoencoder on GPU ---")
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
print(f"Using device: {device}")

class ConvAutoencoder(nn.Module):
    def __init__(self):
        super(ConvAutoencoder, self).__init__()
        # Encoder
        self.encoder = nn.Sequential(
            nn.Conv2d(1, 16, 3, stride=2, padding=1), nn.ReLU(), # -> 16x64x64
            nn.Conv2d(16, 32, 3, stride=2, padding=1), nn.ReLU(), # -> 32x32x32
            nn.Conv2d(32, 64, 7) # -> 64x26x26
        )
        self.fc1 = nn.Linear(64 * 26 * 26, LATENT_DIM)
        # Decoder
        self.fc2 = nn.Linear(LATENT_DIM, 64 * 26 * 26)
        self.decoder = nn.Sequential(
            nn.ConvTranspose2d(64, 32, 7), nn.ReLU(), # -> 32x32x32
            nn.ConvTranspose2d(32, 16, 3, stride=2, padding=1, output_padding=1), nn.ReLU(), # -> 16x64x64
            nn.ConvTranspose2d(16, 1, 3, stride=2, padding=1, output_padding=1), nn.Sigmoid() # -> 1x128x128
        )
    def encode(self, x):
        x = self.encoder(x)
        return self.fc1(x.view(x.size(0), -1))
    def decode(self, z):
        z = self.fc2(z)
        return self.decoder(z.view(z.size(0), 64, 26, 26))
    def forward(self, x):
        return self.decode(self.encode(x))

# Prepare data for PyTorch
tensor_data = torch.from_numpy(fcgr_images).float()
dataset = TensorDataset(tensor_data, tensor_data)
dataloader = DataLoader(dataset, batch_size=BATCH_SIZE, shuffle=True)
model = ConvAutoencoder().to(device)
criterion = nn.MSELoss()
optimizer = optim.Adam(model.parameters(), lr=1e-3)

# Training loop
print("Starting training...")
for epoch in range(EPOCHS):
    total_loss = 0
    for data in dataloader:
        img, _ = data
        img = img.to(device)
        output = model(img)
        loss = criterion(output, img)
        optimizer.zero_grad()
        loss.backward()
        optimizer.step()
        total_loss += loss.item()
    avg_loss = total_loss / len(dataloader)
    print(f"Epoch [{epoch+1}/{EPOCHS}], Average Loss: {avg_loss:.6f}")

print("Training complete.")

model_path = os.path.join(OUTPUT_DIR, f"cnn_autoencoder_{MARKER}.pth")
torch.save(model.state_dict(), model_path)
print(f"Trained model saved to: {model_path}")

del dataloader, tensor_data; gc.collect(); torch.cuda.empty_cache()


# --- 3. EXTRACT LATENT VECTORS AND CLUSTER ---
print("\n--- Step 3: Extracting Latent Vectors and Clustering ---")
inference_dataloader = DataLoader(dataset, batch_size=BATCH_SIZE, shuffle=False)
latent_vectors_list = []
model.eval()
with torch.no_grad():
    for data in inference_dataloader:
        img, _ = data
        img = img.to(device)
        batch_vectors = model.encode(img)
        latent_vectors_list.append(batch_vectors.cpu().numpy())

# Combine the batches of vectors into a single large array
latent_vectors = np.concatenate(latent_vectors_list, axis=0)

print(f"Generated {latent_vectors.shape[0]} latent vectors of dimension {latent_vectors.shape[1]}.")

# Perform clustering
print("Performing HDBSCAN clustering...")
clusterer = hdbscan.HDBSCAN(min_cluster_size=5, min_samples=1, metric='euclidean')
cluster_labels = clusterer.fit_predict(latent_vectors)
num_clusters = len(set(cluster_labels)) - (1 if -1 in cluster_labels else 0)
num_noise = np.sum(cluster_labels == -1)
print(f"Clustering complete. Found {num_clusters} clusters and {num_noise} noise points.")

# Save results
if not os.path.exists(OUTPUT_DIR): os.makedirs(OUTPUT_DIR)
output_path = os.path.join(OUTPUT_DIR, f"cluster_map_{MARKER}_cnn.csv")
cluster_map = pd.DataFrame({'ASV_ID': asv_ids, 'Cluster_ID': cluster_labels})
cluster_map.to_csv(output_path, index=False)

print(f"\n--- SUCCESS: GPU-powered cluster map saved to {output_path} ---")