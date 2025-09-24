import { useState } from 'react';
import { useNotification } from '../../services/contexts/NotificationContext.jsx';
import { graphApi } from '../../services/api.js';

function FileUpload({ onFilesSelected, maxFiles = 1000000, initialFiles = [] }) {
  const { setError } = useNotification();
  const [files, setFiles] = useState(initialFiles);
  const [isUploading, setIsUploading] = useState(false);
  const [stats, setStats] = useState({
    totalFiles: initialFiles.length,
    totalSize: initialFiles.reduce((sum, file) => sum + (file.size || 0), 0),
    zipFiles: initialFiles.filter(file => file.name.toLowerCase().endsWith('.zip')).length,
    imageFiles: initialFiles.filter(file =>
        file.type?.startsWith('image/') || file.name.match(/\.(jpg|jpeg|png)$/i)
    ).length
  });


  const handleFileChange = async (event) => {
    const uploadedFiles = Array.from(event.target.files);
    if (uploadedFiles.length === 0) {
      setIsUploading(false);
      return;
    }
    setIsUploading(true);
    try {
      const fileStats = await graphApi.uploadInput(uploadedFiles);

      console.log(`Uploaded ${fileStats.totalFiles} files`);

      const updatedFiles = [...files].slice(0, maxFiles);
      setFiles(updatedFiles);
      updateParent(fileStats.locations);

      setStats({
        totalFiles: fileStats.totalFiles,
        totalSize: fileStats.totalSize,
        zipFiles: fileStats.zipFiles,
        imageFiles: fileStats.imageFiles
      });
    } catch (error) {
      console.error('Failed to upload files:', error);
      setError(`Error uploading files: ${error.message}`);
    } finally {
      console.log('Finished uploading, resetting isUploading');
      setIsUploading(false);
    }
  };

  const clearFiles = () => {
    setFiles([]);
    setStats({
      totalFiles: 0,
      totalSize: 0,
      zipFiles: 0,
      imageFiles: 0
    });
    updateParent([]);
  };

  const updateParent = (fileList) => {
    if (onFilesSelected) {
      onFilesSelected(fileList);
    }
  };

  // Format file size
  const formatSize = (bytes) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  return (
      <div style={{
        fontFamily: 'Arial, sans-serif',
        fontSize: '12px',
        padding: '8px',
        borderRadius: '4px',
        background: 'rgba(0, 0, 0, 0)',
        maxWidth: '250px',
      }}>
        {/* Header with count and clear button */}
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '6px',
        }}>
          <button
              onClick={clearFiles}
              disabled={isUploading}
              style={{
                padding: '2px 6px',
                fontSize: '10px',
                background: 'rgba(0, 0, 0, 0)',
                color: isUploading ? 'rgba(255, 255, 255, 0.3)' : '#d9534f',
                border: `1px solid ${isUploading ? 'rgba(255, 255, 255, 0.2)' : 'rgba(217, 83, 79, 0.5)'}`,
                borderRadius: '2px',
                cursor: isUploading ? 'not-allowed' : 'pointer',
                transition: 'background 0.2s, color 0.2s, border-color 0.2s',
              }}
              onMouseOver={(e) => !isUploading && (e.target.style.background = 'rgba(217, 83, 79, 0.1)', e.target.style.borderColor = '#d9534f')}
              onMouseOut={(e) => !isUploading && (e.target.style.background = 'rgba(0, 0, 0, 0)', e.target.style.borderColor = 'rgba(217, 83, 79, 0.5)')}
          >
            Clear
          </button>
        </div>
        {/* Stats display */}
        <div style={{
          marginBottom: '8px',
          padding: '8px 10px',
          background: 'rgba(20, 20, 20, 0.9)',
          border: '1px solid rgba(255, 255, 255, 0.15)',
          borderRadius: '4px',
          fontFamily: '"Courier New", Courier, monospace',
          fontSize: 'x',
          color: '#e0e0e0',
          lineHeight: '1',
          boxShadow: '0 2px 4px rgba(0, 0, 0, 0.2)',
          animation: 'fadeIn 0.3s ease-in',
          textAlign: 'left'
        }}>
          <div style={{ marginBottom: '4px' }}>
            <span style={{ color: '#569cd6' }}>total_size</span>: {formatSize(stats.totalSize)}
          </div>
          <div style={{ marginBottom: '4px' }}>
            <span style={{ color: '#569cd6' }}>images</span>: {stats.imageFiles}
          </div>
          <div style={{ marginBottom: '4px' }}>
            <span style={{ color: '#569cd6' }}>zip_files</span>: {stats.zipFiles}
          </div>
          <div>
            <span style={{ color: '#569cd6' }}>other_files</span>: {stats.totalFiles - stats.imageFiles - stats.zipFiles}
          </div>
        </div>
        {/* Upload button */}
        <div style={{
          display: 'flex',
          gap: '6px',
        }}>
          <label style={{
            display: 'inline-block',
            padding: '4px 8px',
            background: 'rgba(0, 0, 0, 0)',
            border: `1px solid ${isUploading ? 'rgba(255, 255, 255, 0.2)' : 'rgba(255, 255, 255, 0.3)'}`,
            borderRadius: '2px',
            cursor: isUploading ? 'not-allowed' : 'pointer',
            textAlign: 'center',
            transition: 'border-color 0.2s',
          }}
                 onMouseOver={(e) => !isUploading && (e.target.style.borderColor = 'rgba(255, 255, 255, 0.5)')}
                 onMouseOut={(e) => !isUploading && (e.target.style.borderColor = 'rgba(255, 255, 255, 0.3)')}
          >
            {isUploading ? 'Uploading...' : (stats.totalFiles > 0 ? 'Add Files' : 'Upload Files')}
            <input
                type="file"
                multiple
                onChange={handleFileChange}
                disabled={isUploading}
                style={{ display: 'none' }}
            />
          </label>
        </div>

        {/* Keyframe animation for fade-in */}
        <style>
          {`
          @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-4px); }
            to { opacity: 1; transform: translateY(0); }
          }
        `}
        </style>
      </div>
  );
}

export default FileUpload;