#!/bin/bash

# Create the test database and grant privileges
PGPASSWORD=edna_password psql -h localhost -U edna_user -d postgres -c "CREATE DATABASE edna_biodiversity_test;"
PGPASSWORD=edna_password psql -h localhost -U edna_user -d postgres -c "GRANT ALL PRIVILEGES ON DATABASE edna_biodiversity_test TO edna_user;"
