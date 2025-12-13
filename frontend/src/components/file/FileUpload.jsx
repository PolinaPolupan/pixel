import { useState } from 'react';
import { useNotification } from '../../services/contexts/NotificationContext.jsx';
import { graphApi } from '../../services/api.js';
import './FileExplorer.css';

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
        const uploadedFiles = Array. from(event.target.files);
        if (uploadedFiles. length === 0) {
            setIsUploading(false);
            return;
        }
        setIsUploading(true);
        try {
            const fileStats = await graphApi.uploadInput(uploadedFiles);

            console.log(`Uploaded ${fileStats.totalFiles} files`);

            const updatedFiles = [... files].slice(0, maxFiles);
            setFiles(updatedFiles);
            updateParent(fileStats.locations);

            setStats({
                totalFiles: fileStats.totalFiles,
                totalSize: fileStats.totalSize,
                zipFiles: fileStats. zipFiles,
                imageFiles:  fileStats.imageFiles
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

    const formatSize = (bytes) => {
        if (bytes < 1024) return `${bytes} B`;
        if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
        return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
    };

    return (
        <div className="file-upload">
            <div className="file-upload-header">
                <button
                    onClick={clearFiles}
                    disabled={isUploading}
                    className="file-upload-clear-btn"
                >
                    Clear
                </button>
            </div>

            <div className="file-upload-stats">
                <div className="file-upload-stat-row">
                    <span className="file-upload-stat-label">total_size</span>: {formatSize(stats.totalSize)}
                </div>
                <div className="file-upload-stat-row">
                    <span className="file-upload-stat-label">images</span>: {stats.imageFiles}
                </div>
                <div className="file-upload-stat-row">
                    <span className="file-upload-stat-label">zip_files</span>: {stats. zipFiles}
                </div>
                <div className="file-upload-stat-row">
                    <span className="file-upload-stat-label">other_files</span>: {stats.totalFiles - stats.imageFiles - stats.zipFiles}
                </div>
            </div>

            <div className="file-upload-actions">
                <label className={`file-upload-label ${isUploading ? 'disabled' : ''}`}>
                    {isUploading ? 'Uploading...' : (stats. totalFiles > 0 ? 'Add Files' : 'Upload Files')}
                    <input
                        type="file"
                        multiple
                        onChange={handleFileChange}
                        disabled={isUploading}
                        className="file-upload-input"
                    />
                </label>
            </div>
        </div>
    );
}

export default FileUpload;