-- Create users table for authentication
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create table for storing analysis results
CREATE TABLE analysis_results (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    marker_type VARCHAR(10) NOT NULL CHECK (marker_type IN ('18S', 'COI')),
    confidence_level VARCHAR(20) NOT NULL CHECK (confidence_level IN ('HIGH', 'MEDIUM', 'LOW', 'POTENTIALLY_NOVEL')),
    species_name VARCHAR(255),
    raw_data_path TEXT,
    analysis_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index on frequently queried fields
CREATE INDEX idx_analysis_marker ON analysis_results(marker_type);
CREATE INDEX idx_analysis_confidence ON analysis_results(confidence_level);
CREATE INDEX idx_analysis_date ON analysis_results(analysis_date);
