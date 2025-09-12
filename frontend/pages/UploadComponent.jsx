import React, { useState, useCallback } from 'react';
import { Upload, File, CheckCircle, AlertCircle, Loader2, Database } from 'lucide-react';
import axios from 'axios';

const UploadComponent = () => {
  const [isDragOver, setIsDragOver] = useState(false);
  const [uploadState, setUploadState] = useState('idle'); // idle, uploading, completed, error
  const [progress, setProgress] = useState(0);
  const [fileName, setFileName] = useState('');
  const [fileSize, setFileSize] = useState('');
  const [uploadedData, setUploadedData] = useState(null);
  const [isLoadingData, setIsLoadingData] = useState(false);

  // Real API call to fetch database information
  const fetchDatabaseInfo = async () => {
    setIsLoadingData(true);

    try {
      console.log('Fetching database information...');
      const response = await axios.get('http://localhost:3001/database-info', {
        timeout: 30000, // 30 second timeout
        headers: {
          'Content-Type': 'application/json',
        }
      });

      console.log('Database info received:', response.data);
      setUploadedData(response.data);

    } catch (error) {
      console.error('Error fetching database info:', error);

      // Handle different types of errors
      let errorMessage = 'Failed to fetch database information';
      let errorDetails = '';

      if (error.code === 'ECONNABORTED') {
        errorMessage = 'Request timed out';
        errorDetails = 'The database server took too long to respond. Please try again.';
      } else if (error.response) {
        // Server responded with error status
        errorMessage = `Server Error (${error.response.status})`;
        errorDetails = error.response.data?.message || error.response.data?.error || 'Unknown server error';
      } else if (error.request) {
        // Request was made but no response received
        errorMessage = 'Connection Error';
        errorDetails = 'Could not connect to the database server. Please ensure the server is running on port 3001.';
      } else {
        // Something else happened
        errorMessage = 'Unexpected Error';
        errorDetails = error.message;
      }

      setUploadedData({
        error: true,
        message: errorMessage,
        details: errorDetails,
        timestamp: new Date().toISOString()
      });
    } finally {
      setIsLoadingData(false);
    }
  };

  // Simulate realistic upload progress with occasional pauses
  const simulateUpload = useCallback((file) => {
    setUploadState('uploading');
    setFileName(file.name);
    setFileSize((file.size / (1024 * 1024)).toFixed(2) + ' MB');
    setProgress(0);

    // Realistic progress simulation with pauses
    const progressSteps = [
      { progress: 5, delay: 200 },
      { progress: 15, delay: 400 },
      { progress: 25, delay: 300 },
      { progress: 35, delay: 800 }, // Pause here
      { progress: 45, delay: 200 },
      { progress: 55, delay: 600 }, // Another pause
      { progress: 65, delay: 300 },
      { progress: 75, delay: 1200 }, // Longer pause
      { progress: 85, delay: 400 },
      { progress: 95, delay: 500 },
      { progress: 100, delay: 300 }
    ];

    let currentStep = 0;
    const updateProgress = () => {
      if (currentStep < progressSteps.length) {
        const step = progressSteps[currentStep];
        setTimeout(() => {
          setProgress(step.progress);
          if (step.progress === 100) {
            setTimeout(() => {
              setUploadState('completed');
              fetchDatabaseInfo(); // Call real API instead of mock data
            }, 500);
          } else {
            currentStep++;
            updateProgress();
          }
        }, step.delay);
      }
    };

    updateProgress();
  }, []);

  const handleDragOver = (e) => {
    e.preventDefault();
    setIsDragOver(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    setIsDragOver(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragOver(false);

    const files = Array.from(e.dataTransfer.files);
    const fastqFile = files.find(file =>
      file.name.toLowerCase().endsWith('.fastq.gz') ||
      file.name.toLowerCase().endsWith('.fq.gz')
    );

    if (fastqFile) {
      simulateUpload(fastqFile);
    } else {
      setUploadState('error');
      setTimeout(() => setUploadState('idle'), 3000);
    }
  };

  const handleFileSelect = (e) => {
    const file = e.target.files[0];
    if (file && (file.name.toLowerCase().endsWith('.fastq.gz') ||
                 file.name.toLowerCase().endsWith('.fq.gz'))) {
      simulateUpload(file);
    } else {
      setUploadState('error');
      setTimeout(() => setUploadState('idle'), 3000);
    }
  };

  const resetUpload = () => {
    setUploadState('idle');
    setProgress(0);
    setFileName('');
    setFileSize('');
    setUploadedData(null);
  };

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      <div className="text-center mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">Biodiversity Database Explorer</h1>
        <p className="text-gray-600">Upload FASTQ.gz files or explore the biodiversity database</p>
      </div>

      {/* Upload Area */}
      <div
        className={`border-2 border-dashed rounded-lg p-8 text-center transition-colors ${
          isDragOver
            ? 'border-blue-400 bg-blue-50'
            : uploadState === 'error'
            ? 'border-red-400 bg-red-50'
            : uploadState === 'completed'
            ? 'border-green-400 bg-green-50'
            : 'border-gray-300 bg-gray-50 hover:border-gray-400'
        }`}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
      >
        {uploadState === 'idle' && (
          <>
            <Upload className="mx-auto h-12 w-12 text-gray-400 mb-4" />
            <div className="space-y-2">
              <p className="text-lg font-medium text-gray-700">
                Drop your FASTQ.gz files here
              </p>
              <p className="text-sm text-gray-500">
                or click to browse files
              </p>
              <input
                type="file"
                accept=".fastq.gz,.fq.gz"
                onChange={handleFileSelect}
                className="hidden"
                id="file-upload"
              />
              <label
                htmlFor="file-upload"
                className="inline-block px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 cursor-pointer"
              >
                Select Files
              </label>
            </div>
          </>
        )}

        {uploadState === 'uploading' && (
          <div className="space-y-4">
            <Loader2 className="mx-auto h-12 w-12 text-blue-500 animate-spin" />
            <div className="space-y-2">
              <div className="flex items-center justify-center gap-2">
                <File className="h-5 w-5 text-gray-600" />
                <span className="font-medium text-gray-700">{fileName}</span>
                <span className="text-sm text-gray-500">({fileSize})</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-3">
                <div
                  className="bg-blue-500 h-3 rounded-full transition-all duration-300"
                  style={{ width: `${progress}%` }}
                />
              </div>
              <p className="text-sm text-gray-600">{progress}% uploaded</p>
            </div>
          </div>
        )}

        {uploadState === 'completed' && (
          <div className="space-y-4">
            <CheckCircle className="mx-auto h-12 w-12 text-green-500" />
            <div>
              <p className="text-lg font-medium text-green-700">Upload Complete!</p>
              <p className="text-sm text-gray-600">{fileName} ({fileSize})</p>
            </div>
            <button
              onClick={resetUpload}
              className="px-4 py-2 bg-gray-500 text-white rounded-md hover:bg-gray-600"
            >
              Upload Another File
            </button>
          </div>
        )}

        {uploadState === 'error' && (
          <>
            <AlertCircle className="mx-auto h-12 w-12 text-red-500 mb-4" />
            <p className="text-lg font-medium text-red-700">Upload Failed</p>
            <p className="text-sm text-gray-600">Please upload a valid FASTQ.gz file</p>
          </>
        )}
      </div>

      {/* Manual Database Fetch Button */}
      {uploadState === 'idle' && !uploadedData && !isLoadingData && (
        <div className="bg-white rounded-lg border border-gray-200 p-6 text-center">
          <Database className="mx-auto h-12 w-12 text-gray-400 mb-4" />
          <h3 className="text-lg font-medium text-gray-700 mb-2">View Database Information</h3>
          <p className="text-sm text-gray-500 mb-4">
            Explore the current biodiversity database without uploading a file
          </p>
          <button
            onClick={fetchDatabaseInfo}
            disabled={isLoadingData}
            className="px-6 py-2 bg-green-500 text-white rounded-md hover:bg-green-600 disabled:opacity-50 disabled:cursor-not-allowed flex items-center space-x-2 mx-auto"
          >
            <Database className="h-4 w-4" />
            <span>Explore Database</span>
          </button>
        </div>
      )}

      {/* Data Loading */}
      {isLoadingData && (
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <div className="flex items-center justify-center space-x-2">
            <Loader2 className="h-5 w-5 animate-spin text-blue-500" />
            <span className="text-gray-600">Fetching database information...</span>
          </div>
        </div>
      )}

      {/* Refresh Database Info Button */}
      {uploadedData && !isLoadingData && (
        <div className="flex justify-center">
          <button
            onClick={fetchDatabaseInfo}
            disabled={isLoadingData}
            className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 disabled:opacity-50 disabled:cursor-not-allowed flex items-center space-x-2"
          >
            <span>Refresh Database Info</span>
          </button>
        </div>
      )}

      {/* Results Display */}
      {uploadedData && !isLoadingData && (
        <div className="space-y-6">
          <h2 className="text-2xl font-bold text-gray-900">Biodiversity Database Information</h2>

          {uploadedData.error ? (
            <div className="bg-red-50 border border-red-200 rounded-lg p-6">
              <div className="flex items-center space-x-2 text-red-800">
                <AlertCircle className="h-5 w-5" />
                <span className="font-medium">Database Connection Error</span>
              </div>
              <p className="text-red-600 mt-2">{uploadedData.message}</p>
              {uploadedData.details && (
                <p className="text-sm text-red-500 mt-1">{uploadedData.details}</p>
              )}
            </div>
          ) : (
            <>
              {/* Database Overview */}
              {uploadedData.info && (
                <div className="bg-white rounded-lg border border-gray-200 p-6">
                  <h3 className="text-lg font-semibold text-gray-800 mb-4">Database Overview</h3>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    <div className="text-center">
                      <div className="text-2xl font-bold text-blue-600">
                        {uploadedData.info.totalTables || 0}
                      </div>
                      <div className="text-sm text-gray-600">Total Tables</div>
                    </div>
                    <div className="text-center">
                      <div className="text-2xl font-bold text-green-600">
                        {uploadedData.info.totalRecords?.toLocaleString() || 0}
                      </div>
                      <div className="text-sm text-gray-600">Total Records</div>
                    </div>
                    <div className="text-center">
                      <div className="text-2xl font-bold text-purple-600">
                        {uploadedData.info.databaseSize || 'N/A'}
                      </div>
                      <div className="text-sm text-gray-600">Database Size</div>
                    </div>
                    <div className="text-center">
                      <div className="text-2xl font-bold text-orange-600">
                        {uploadedData.info.version || 'PostgreSQL'}
                      </div>
                      <div className="text-sm text-gray-600">Database</div>
                    </div>
                  </div>
                  {uploadedData.info.databaseName && (
                    <div className="mt-4 text-center">
                      <span className="text-sm text-gray-600">Database: </span>
                      <span className="font-medium text-gray-800">{uploadedData.info.databaseName}</span>
                    </div>
                  )}
                </div>
              )}

              {/* Biodiversity Insights */}
              {uploadedData.biodiversityInsights && (
                <div className="bg-gradient-to-r from-green-50 to-blue-50 rounded-lg border border-green-200 p-6">
                  <h3 className="text-lg font-semibold text-gray-800 mb-4">Biodiversity Data Insights</h3>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    {uploadedData.biodiversityInsights.totalASVs && (
                      <div className="bg-white rounded-lg p-4 border border-green-100">
                        <div className="text-2xl font-bold text-green-600">
                          {uploadedData.biodiversityInsights.totalASVs.toLocaleString()}
                        </div>
                        <div className="text-sm text-gray-600">Total ASVs</div>
                      </div>
                    )}
                    <div className="bg-white rounded-lg p-4 border border-blue-100">
                      <div className="text-2xl font-bold text-blue-600">
                        {uploadedData.biodiversityInsights.totalSamples?.toLocaleString() || '0'}
                      </div>
                      <div className="text-sm text-gray-600">Total Samples</div>
                    </div>
                    {uploadedData.biodiversityInsights.markerTypes && (
                      <div className="bg-white rounded-lg p-4 border border-purple-100">
                        <div className="text-sm font-medium text-gray-700 mb-2">Marker Types:</div>
                        <div className="flex flex-wrap gap-1">
                          {uploadedData.biodiversityInsights.markerTypes.map((marker, index) => (
                            <span key={index} className="text-xs bg-purple-100 text-purple-800 px-2 py-1 rounded">
                              {marker}
                            </span>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              )}

              {/* Tables List */}
              {uploadedData.tables && uploadedData.tables.length > 0 && (
                <div className="bg-white rounded-lg border border-gray-200 p-6">
                  <h3 className="text-lg font-semibold text-gray-800 mb-4">Database Tables ({uploadedData.tables.length})</h3>
                  <div className="space-y-4">
                    {uploadedData.tables.map((table, index) => (
                      <div key={index} className="border border-gray-100 rounded-lg p-4 hover:border-gray-200 transition-colors">
                        <div className="flex items-center justify-between mb-3">
                          <h4 className="font-medium text-gray-800 text-lg">{table.tableName}</h4>
                          <div className="flex items-center space-x-4">
                            <span className="text-sm bg-blue-100 text-blue-800 px-3 py-1 rounded-full">
                              {table.rowCount?.toLocaleString() || 0} rows
                            </span>
                            <span className="text-sm bg-gray-100 text-gray-700 px-3 py-1 rounded-full">
                              {table.columns?.length || 0} columns
                            </span>
                          </div>
                        </div>

                        {table.error && (
                          <div className="bg-red-50 border border-red-200 rounded p-2 mb-3">
                            <span className="text-sm text-red-600">Error: {table.error}</span>
                          </div>
                        )}

                        {table.columns && table.columns.length > 0 && (
                          <div className="mb-4">
                            <p className="text-sm font-medium text-gray-600 mb-2">Columns:</p>
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-2">
                              {table.columns.map((col, colIndex) => (
                                <div key={colIndex} className="text-xs bg-gray-50 border rounded px-3 py-2">
                                  <div className="font-medium text-gray-800">{col.column_name}</div>
                                  <div className="text-gray-600">
                                    {col.data_type} {col.is_nullable === 'YES' && '(nullable)'}
                                  </div>
                                </div>
                              ))}
                            </div>
                          </div>
                        )}

                        {table.sampleData && table.sampleData.length > 0 && (
                          <div>
                            <p className="text-sm font-medium text-gray-600 mb-2">Sample Data (first {table.sampleData.length} rows):</p>
                            <div className="overflow-x-auto bg-gray-50 rounded border">
                              <table className="min-w-full text-xs">
                                <thead>
                                  <tr className="bg-gray-100">
                                    {Object.keys(table.sampleData[0]).map((key, keyIndex) => (
                                      <th key={keyIndex} className="px-3 py-2 text-left font-medium text-gray-700 border-b whitespace-nowrap">
                                        {key}
                                      </th>
                                    ))}
                                  </tr>
                                </thead>
                                <tbody>
                                  {table.sampleData.slice(0, 5).map((row, rowIndex) => (
                                    <tr key={rowIndex} className="border-b hover:bg-gray-50">
                                      {Object.values(row).map((value, valueIndex) => (
                                        <td key={valueIndex} className="px-3 py-2 text-gray-800">
                                          <div className="max-w-xs truncate" title={String(value)}>
                                            {value === null ? (
                                              <span className="text-gray-400 italic">null</span>
                                            ) : String(value).length > 50 ? (
                                              String(value).substring(0, 50) + '...'
                                            ) : (
                                              String(value)
                                            )}
                                          </div>
                                        </td>
                                      ))}
                                    </tr>
                                  ))}
                                </tbody>
                              </table>
                            </div>
                          </div>
                        )}

                        {(!table.sampleData || table.sampleData.length === 0) && !table.error && (
                          <div className="text-sm text-gray-500 italic">No sample data available</div>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Raw Data Display */}
              {uploadedData.rawData && (
                <div className="bg-white rounded-lg border border-gray-200 p-6">
                  <h3 className="text-lg font-semibold text-gray-800 mb-4">Technical Details</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                    {uploadedData.rawData.fullVersion && (
                      <div>
                        <span className="text-sm font-medium text-gray-600">Database Version:</span>
                        <p className="text-sm text-gray-800 mt-1">{uploadedData.rawData.fullVersion}</p>
                      </div>
                    )}
                    {uploadedData.rawData.connectionInfo && (
                      <div>
                        <span className="text-sm font-medium text-gray-600">Connection Info:</span>
                        <div className="text-sm text-gray-800 mt-1">
                          {Object.entries(uploadedData.rawData.connectionInfo).map(([key, value]) => (
                            <div key={key}>
                              <span className="capitalize">{key}:</span> {String(value)}
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>

                  <details className="cursor-pointer">
                    <summary className="text-sm font-medium text-gray-600 hover:text-gray-800">
                      View Raw Response Data
                    </summary>
                    <pre className="bg-gray-50 p-4 rounded text-xs overflow-x-auto text-gray-700 mt-2 max-h-96">
                      {JSON.stringify(uploadedData.rawData, null, 2)}
                    </pre>
                  </details>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default UploadComponent;