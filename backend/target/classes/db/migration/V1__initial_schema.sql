CREATE TABLE samples (
    sample_id VARCHAR(50) PRIMARY KEY,
    latitude DECIMAL(9,6),
    longitude DECIMAL(9,6),
    collection_date DATE,
    site_name VARCHAR(255),
    depth_meters DECIMAL(6,2),
    temperature_celsius DECIMAL(5,2),
    ph DECIMAL(4,2),
    salinity_ppt DECIMAL(5,2),
    environmental_conditions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE taxa (
    taxon_id SERIAL PRIMARY KEY,
    cluster_id INTEGER NOT NULL,
    marker_type VARCHAR(10) NOT NULL CHECK (marker_type IN ('18S', 'COI')),
    asv_sequence TEXT,
    sequence_length INTEGER,
    gc_content DECIMAL(5,2),
    annotation_name VARCHAR(255),
    taxonomy_kingdom VARCHAR(100),
    taxonomy_phylum VARCHAR(100),
    taxonomy_class VARCHAR(100),
    taxonomy_order VARCHAR(100),
    taxonomy_family VARCHAR(100),
    taxonomy_genus VARCHAR(100),
    taxonomy_species VARCHAR(255),
    confidence_level VARCHAR(50) CHECK (confidence_level IN ('High', 'Medium', 'Low', 'Potentially Novel')),
    blast_identity DECIMAL(5,2),
    blast_evalue DECIMAL(10,2),
    blast_bitscore DECIMAL(10,2),
    phylogenetic_placement TEXT,
    is_novel_lineage BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE abundance (
    abundance_id SERIAL PRIMARY KEY,
    sample_id VARCHAR(50) NOT NULL REFERENCES samples(sample_id) ON DELETE CASCADE,
    taxon_id INTEGER NOT NULL REFERENCES taxa(taxon_id) ON DELETE CASCADE,
    raw_count INTEGER NOT NULL DEFAULT 0,
    corrected_count DECIMAL(12,4) NOT NULL DEFAULT 0,
    relative_abundance DECIMAL(8,6) NOT NULL DEFAULT 0,
    bias_correction_factor DECIMAL(8,4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pipeline_runs (
    run_id SERIAL PRIMARY KEY,
    run_name VARCHAR(255) NOT NULL,
    marker_type VARCHAR(10) NOT NULL,
    status VARCHAR(50) DEFAULT 'RUNNING' CHECK (status IN ('RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    total_samples INTEGER,
    total_asvs INTEGER,
    total_clusters INTEGER,
    pipeline_version VARCHAR(50),
    notes TEXT,
    created_by VARCHAR(100)
);

-- Indexes for samples table
CREATE INDEX idx_samples_collection_date ON samples(collection_date);
CREATE INDEX idx_samples_location ON samples(latitude, longitude);
CREATE INDEX idx_samples_site_name ON samples(site_name);

-- Unique constraint and indexes for taxa table
CREATE UNIQUE INDEX uk_taxa_cluster_marker ON taxa(cluster_id, marker_type);
CREATE INDEX idx_taxa_marker_type ON taxa(marker_type);
CREATE INDEX idx_taxa_confidence ON taxa(confidence_level);
CREATE INDEX idx_taxa_annotation ON taxa(annotation_name);
CREATE INDEX idx_taxa_kingdom ON taxa(taxonomy_kingdom);
CREATE INDEX idx_taxa_novel ON taxa(is_novel_lineage);

-- Unique constraint and indexes for abundance table
CREATE UNIQUE INDEX uk_abundance_sample_taxon ON abundance(sample_id, taxon_id);
CREATE INDEX idx_abundance_sample_id ON abundance(sample_id);
CREATE INDEX idx_abundance_taxon_id ON abundance(taxon_id);
CREATE INDEX idx_abundance_relative ON abundance(relative_abundance);

-- Indexes for pipeline_runs table
CREATE INDEX idx_pipeline_runs_status ON pipeline_runs(status);
CREATE INDEX idx_pipeline_runs_marker ON pipeline_runs(marker_type);
CREATE INDEX idx_pipeline_runs_started ON pipeline_runs(started_at);
