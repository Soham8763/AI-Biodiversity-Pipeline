const express = require('express');
const { Client } = require('pg');
const cors = require('cors');
const dotenv = require('dotenv');
dotenv.config();

const app = express();
app.use(cors());
app.use(express.json());

const dbConfig = {
  connectionString: process.env.DATABASE_URL,
  ssl: {
    rejectUnauthorized: false
  },
  connectionTimeoutMillis: 10000,
  idleTimeoutMillis: 30000,
  max: 10,
};

const alternativeDbConfig = {
  host: process.env.DB_HOST,
  port: process.env.DB_PORT || 5432,
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  ssl: {
    rejectUnauthorized: false
  },
  connectionTimeoutMillis: 10000,
  idleTimeoutMillis: 30000,
};

app.get('/test-connection', async (req, res) => {
  const client = new Client(dbConfig);

  try {
    console.log('Testing database connection...');
    await client.connect();
    console.log('Connected successfully');

    const result = await client.query('SELECT NOW() as current_time');
    console.log('Query executed successfully');

    res.json({
      success: true,
      message: 'Database connection successful',
      currentTime: result.rows[0].current_time,
      connectionConfig: {
        host: client.host,
        port: client.port,
        database: client.database,
        user: client.user
      }
    });

  } catch (error) {
    console.error('Connection test failed:', error);
    res.status(500).json({
      success: false,
      error: error.message,
      code: error.code,
      details: error.detail || 'No additional details'
    });
  } finally {
    try {
      await client.end();
      console.log('Client disconnected');
    } catch (endError) {
      console.error('Error disconnecting client:', endError);
    }
  }
});

app.get('/database-info', async (req, res) => {
  let client;

  try {
    console.log('Creating new database client...');
    client = new Client(dbConfig);

    console.log('Attempting to connect to database...');
    await client.connect();
    console.log('Successfully connected to database');

    console.log('Fetching database version...');
    const versionResult = await client.query('SELECT version()');
    const fullVersion = versionResult.rows[0].version;

    console.log('Fetching database info...');
    const dbInfoResult = await client.query(`
      SELECT
        current_database() as database_name,
        pg_size_pretty(pg_database_size(current_database())) as database_size
    `);

    // Get tables information
    console.log('Fetching tables information...');
    const tablesResult = await client.query(`
      SELECT
        t.table_name,
        COALESCE(s.n_tup_ins + s.n_tup_upd + s.n_tup_del, 0) as row_count
      FROM information_schema.tables t
      LEFT JOIN pg_stat_user_tables s ON s.relname = t.table_name
      WHERE t.table_schema = 'public'
        AND t.table_type = 'BASE TABLE'
      ORDER BY t.table_name
    `);

    console.log(`Found ${tablesResult.rows.length} tables`);

    const tablesWithDetails = [];

    for (const table of tablesResult.rows) {
      console.log(`Processing table: ${table.table_name}`);

      try {
        const columnsResult = await client.query(`
          SELECT
            column_name,
            data_type,
            is_nullable,
            column_default
          FROM information_schema.columns
          WHERE table_schema = 'public'
            AND table_name = $1
          ORDER BY ordinal_position
        `, [table.table_name]);

        const rowCountResult = await client.query(`
          SELECT COUNT(*) as exact_count FROM "${table.table_name}"
        `);

        const sampleDataResult = await client.query(`
          SELECT * FROM "${table.table_name}" LIMIT 3
        `);

        tablesWithDetails.push({
          tableName: table.table_name,
          rowCount: parseInt(rowCountResult.rows[0].exact_count),
          columns: columnsResult.rows,
          sampleData: sampleDataResult.rows
        });

      } catch (tableError) {
        console.error(`Error processing table ${table.table_name}:`, tableError.message);
        tablesWithDetails.push({
          tableName: table.table_name,
          rowCount: 0,
          columns: [],
          sampleData: [],
          error: tableError.message
        });
      }
    }

    const totalRecords = tablesWithDetails.reduce((sum, table) => sum + (table.rowCount || 0), 0);

    const biodiversityInsights = {};

    const asvTable = tablesWithDetails.find(t =>
      t.tableName.toLowerCase().includes('asv') ||
      t.tableName.toLowerCase().includes('otu') ||
      t.tableName.toLowerCase().includes('sequence')
    );

    const sampleTable = tablesWithDetails.find(t =>
      t.tableName.toLowerCase().includes('sample') ||
      t.tableName.toLowerCase().includes('metadata')
    );

    if (asvTable) {
      biodiversityInsights.totalASVs = asvTable.rowCount;
    }

    if (sampleTable) {
      biodiversityInsights.totalSamples = sampleTable.rowCount;
    }

    const markerTypes = new Set();
    tablesWithDetails.forEach(table => {
      table.columns.forEach(col => {
        const colName = col.column_name.toLowerCase();
        if (colName.includes('16s')) markerTypes.add('16S rRNA');
        if (colName.includes('its')) markerTypes.add('ITS');
        if (colName.includes('18s')) markerTypes.add('18S rRNA');
        if (colName.includes('coi')) markerTypes.add('COI');
        if (colName.includes('marker')) markerTypes.add('Genetic Marker');
      });
    });

    if (markerTypes.size > 0) {
      biodiversityInsights.markerTypes = Array.from(markerTypes);
    }

    const response = {
      success: true,
      info: {
        databaseName: dbInfoResult.rows[0].database_name,
        databaseSize: dbInfoResult.rows[0].database_size,
        version: fullVersion.split(' ')[0] + ' ' + fullVersion.split(' ')[1],
        totalTables: tablesWithDetails.length,
        totalRecords: totalRecords
      },
      tables: tablesWithDetails,
      biodiversityInsights: Object.keys(biodiversityInsights).length > 0 ? biodiversityInsights : null,
      rawData: {
        fullVersion: fullVersion,
        connectionInfo: {
          host: client.host,
          port: client.port,
          database: client.database,
          user: client.user
        }
      }
    };

    console.log('Database info retrieved successfully');
    res.json(response);

  } catch (error) {
    console.error('Database operation failed:', error);

    const errorResponse = {
      success: false,
      error: true,
      message: error.message,
      code: error.code,
      details: error.detail || 'Connection failed - please check database credentials and network connectivity',
      timestamp: new Date().toISOString()
    };

    if (error.code === 'ECONNRESET') {
      errorResponse.suggestion = 'The database connection was reset. This could be due to network issues, SSL configuration, or database server problems.';
    } else if (error.code === 'ENOTFOUND') {
      errorResponse.suggestion = 'Database host not found. Please check the hostname in your connection string.';
    } else if (error.code === '28P01') {
      errorResponse.suggestion = 'Authentication failed. Please check your username and password.';
    } else if (error.code === 'ETIMEDOUT') {
      errorResponse.suggestion = 'Connection timed out. The database server might be unreachable or overloaded.';
    }

    res.status(500).json(errorResponse);

  } finally {
    if (client) {
      try {
        await client.end();
        console.log('Database client disconnected successfully');
      } catch (endError) {
        console.error('Error disconnecting database client:', endError);
      }
    }
  }
});

app.get('/health', (req, res) => {
  res.json({
    status: 'Server is running',
    timestamp: new Date().toISOString(),
    uptime: process.uptime()
  });
});

app.use((error, req, res, next) => {
  console.error('Unhandled error:', error);
  res.status(500).json({
    error: 'Internal server error',
    message: error.message
  });
});

const PORT = process.env.PORT || 3001;

app.listen(PORT, () => {
  console.log(`Proxy server running on port ${PORT}`);
  console.log(`Health check: http://localhost:${PORT}/health`);
  console.log(`Test connection: http://localhost:${PORT}/test-connection`);
  console.log(`Database info: http://localhost:${PORT}/database-info`);
});

module.exports = app;