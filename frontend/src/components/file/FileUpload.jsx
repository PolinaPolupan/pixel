import { useState } from 'react';
import { useScene } from '../../services/contexts/SceneContext.jsx';
import { useNotification } from '../../services/contexts/NotificationContext.jsx';
import { sceneApi } from '../../services/api.js';

/**
 * File upload component for MyPixel
 * Allows uploading images and ZIP files to the current scene
 *
 * @param {Function} onFilesSelected - Callback when files are selected/changed
 * @param {number} maxFiles - Maximum number of files allowed
 * @param {Array} initialFiles - Initial file list
 */
function FileUpload({ onFilesSelected, maxFiles = 1000000, initialFiles = [] }) {
  const { sceneId } = useScene();
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

  // Handle file upload and send to server
  const handleFileChange = async (event) => {
    const uploadedFiles = Array.from(event.target.files);
    if (uploadedFiles.length === 0) {
      setIsUploading(false);
      return;
    }

    // Validate file types before uploading
    const validFiles = uploadedFiles.filter(file => {
      const isImage = file.type === 'image/jpeg' || file.type === 'image/png' ||
          file.name.match(/\.(jpg|jpeg|png)$/i);
      const isZip = file.type === 'application/zip' ||
          file.type === 'application/x-zip-compressed' ||
          file.name.toLowerCase().endsWith('.zip');
      return isImage || isZip;
    });

    if (validFiles.length === 0) {
      setError('Please select only JPEG, PNG, or ZIP files.');
      setIsUploading(false);
      return;
    }

    if (validFiles.length < uploadedFiles.length) {
      setError('Some files were skipped because only JPEG, PNG, and ZIP files are allowed.');
    }

    setIsUploading(true);
    try {
      // Use the centralized API client for uploading
      const serverLocations = await sceneApi.uploadInput(sceneId, validFiles);

      // Create file data for all server-returned paths
      const updatedFileData = serverLocations.map((location, index) => {
        const serverUrl = location.fileLocation || location; // Handle object or string
        // Use the original file's metadata for the first file if available
        const originalFile = validFiles[Math.min(index, validFiles.length - 1)];
        return {
          id: Date.now() + Math.random().toString(36).substr(2, 9) + index,
          name: serverUrl.substring(serverUrl.lastIndexOf('/') + 1), // Extract filename for stats
          size: originalFile ? originalFile.size : 0, // Approximate size (unknown for ZIP contents)
          type: originalFile ? originalFile.type : 'image/jpeg', // Default to JPEG for ZIP contents
          fullUrl: sceneApi.getFileUrl(sceneId, serverUrl),
          serverUrl: serverUrl // Store full server path
        };
      });

      console.log(`Uploaded ${validFiles.length} files, server returned ${serverLocations.length} paths`);

      const updatedFiles = [...files, ...updatedFileData].slice(0, maxFiles);
      setFiles(updatedFiles);

      // Update stats based on server-returned paths
      setStats({
        totalFiles: updatedFiles.length,
        totalSize: updatedFiles.reduce((sum, file) => sum + (file.size || 0), 0),
        zipFiles: updatedFiles.filter(file => file.name.toLowerCase().endsWith('.zip')).length,
        imageFiles: updatedFiles.filter(file =>
            file.type?.startsWith('image/') || file.name.match(/\.(jpg|jpeg|png)$/i)
        ).length
      });

      updateParent(updatedFiles);

    } catch (error) {
      console.error('Failed to upload files:', error);
      setError(`Error uploading files: ${error.message}`);
    } finally {
      console.log('Finished uploading, resetting isUploading');
      setIsUploading(false);
    }
  };

  // Clear all files
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

  // Update parent with full server paths
  const updateParent = (fileList) => {
    if (onFilesSelected) {
      const filePaths = fileList.map(file => file.serverUrl); // Use full server paths
      onFilesSelected(filePaths);
    }
  };

  // Format file count label
  const getFileLabel = () => {
    if (stats.totalFiles === 0) return "No files";
    if (stats.totalFiles === 1) return "1 file";
    return `${stats.totalFiles} files`;
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
        {stats.totalFiles > 0 && (
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: '6px',
            }}>
              <span style={{ fontWeight: 500 }}>{getFileLabel()}</span>
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
        )}

        {/* Stats display */}
        {stats.totalFiles > 0 && (
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
        )}

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
                accept=".jpg,.jpeg,.png,.zip"
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