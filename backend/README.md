# eDNA Biodiversity Pipeline Backend

A Spring Boot-based REST API backend for AI-enhanced eDNA biodiversity analysis pipeline.

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- PostgreSQL 14+
- Maven
- Docker
- JUnit 5
- Testcontainers

## Features

- Complete CRUD operations for Samples, Taxa, and Abundance data
- Advanced search capabilities with filtering and pagination
- JWT-based authentication and authorization
- OpenAPI/Swagger documentation
- Comprehensive test coverage
- Docker containerization

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose
- PostgreSQL 14+ (if running locally)

## Setup and Installation

## Database Schema

### Tables Overview

#### 1. Samples Table
```sql
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
```

#### 2. Taxa Table
```sql
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
    confidence_level VARCHAR(50),
    blast_identity DECIMAL(5,2),
    blast_evalue DECIMAL(10,2),
    blast_bitscore DECIMAL(10,2),
    phylogenetic_placement TEXT,
    is_novel_lineage BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 3. Abundance Table
```sql
CREATE TABLE abundance (
    abundance_id SERIAL PRIMARY KEY,
    sample_id VARCHAR(50) NOT NULL REFERENCES samples(sample_id),
    taxon_id INTEGER NOT NULL REFERENCES taxa(taxon_id),
    raw_count INTEGER NOT NULL DEFAULT 0,
    corrected_count DECIMAL(12,4) NOT NULL DEFAULT 0,
    relative_abundance DECIMAL(8,6) NOT NULL DEFAULT 0,
    bias_correction_factor DECIMAL(8,4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Local Development Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/Soham8763/AI-Biodiversity-Pipeline.git
   cd AI-Biodiversity-Pipeline/backend
   ```

2. Configure the database:
   ```bash
   # Start PostgreSQL container
   docker compose up -d db

   # Wait for container to be ready
   sleep 5

   # Create test database
   docker exec -it biodiversity-db psql -U edna_user -d postgres -c "CREATE DATABASE edna_biodiversity_test;"
   ```

3. Configure application properties:
   - Main configuration: `src/main/resources/application.yml`
   - Test configuration: `src/test/resources/application-test.yml`

3. Build the application:
   ```bash
   mvn clean install
   ```

4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Docker Setup

1. Build and run using Docker Compose:
   ```bash
   docker-compose up --build
   ```

The application will be available at `http://localhost:8080`

## API Documentation

Once the application is running, you can access:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI docs: `http://localhost:8080/v3/api-docs`

## Testing

Run tests with:
```bash
mvn test
```

The project includes:
- Unit tests for all services
- Integration tests for controllers
- Testcontainers for database testing

## Security

- JWT-based authentication
- Role-based access control
- Secure password hashing
- CORS configuration

## Configuration Details

### application.yml
```yaml
spring:
  application:
    name: edna-biodiversity-pipeline
  datasource:
    url: jdbc:postgresql://localhost:5432/edna_biodiversity
    username: ${DB_USERNAME:edna_user}
    password: ${DB_PASSWORD:edna_password}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

edna:
  pipeline:
    markers:
      - 18S
      - COI
    confidence-levels:
      - HIGH
      - MEDIUM
      - LOW
      - POTENTIALLY_NOVEL
```

### application-test.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/edna_biodiversity_test
    username: ${DB_USERNAME:edna_user}
    password: ${DB_PASSWORD:edna_password}
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  flyway:
    enabled: false
```

## Troubleshooting

### Common Issues

1. **Database Connection Issues**
   ```
   FATAL: role "edna_user" does not exist
   ```
   Solution: Create the database user and grant permissions:
   ```sql
   CREATE USER edna_user WITH PASSWORD 'edna_password';
   ALTER USER edna_user WITH SUPERUSER;
   ```

2. **Test Database Issues**
   - Ensure test database exists
   - Verify test configuration in `application-test.yml`
   - Check that PostgreSQL container is running

3. **Build Failures**
   - Clean Maven cache: `mvn clean`
   - Update dependencies: `mvn versions:display-dependency-updates`
   - Verify Java version: `java -version`

## API Endpoints

### Authentication
- POST `/api/v1/auth/register` - Register new user
- POST `/api/v1/auth/login` - Authenticate user

### Samples
- GET `/api/v1/samples` - List all samples
- GET `/api/v1/samples/{id}` - Get sample by ID
- POST `/api/v1/samples` - Create new sample
- PUT `/api/v1/samples/{id}` - Update sample
- DELETE `/api/v1/samples/{id}` - Delete sample
- GET `/api/v1/samples/search` - Search samples with filters

### Taxa
- GET `/api/v1/taxa` - List all taxa
- GET `/api/v1/taxa/{id}` - Get taxon by ID
- POST `/api/v1/taxa` - Create new taxon
- PUT `/api/v1/taxa/{id}` - Update taxon
- DELETE `/api/v1/taxa/{id}` - Delete taxon
- GET `/api/v1/taxa/search` - Search taxa with filters

### Abundance
- GET `/api/v1/abundance` - List all abundance records
- GET `/api/v1/abundance/{id}` - Get abundance by ID
- POST `/api/v1/abundance` - Create new abundance record
- PUT `/api/v1/abundance/{id}` - Update abundance record
- DELETE `/api/v1/abundance/{id}` - Delete abundance record
- GET `/api/v1/abundance/search` - Search abundance records with filters

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
