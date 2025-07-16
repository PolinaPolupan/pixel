import React from 'react';
import { IoReload } from 'react-icons/io5';
import { FileTree } from './file-explorer/FileTree';
import { FilePreview } from './file-explorer/FilePreview';
import { useFileExplorer } from '../hooks/useFileExplorer';

/**
 * File Explorer component with improved modular structure
 */
const FileExplorer = () => {
    const {
        items,
        previewItem,
        previewContent,
        refreshItems,
        toggleFolder,
        handleFileClick,
        closePreview,
        downloadAsZip
    } = useFileExplorer();

    return (
        <>
            <div
                style={{
                    height: '100%',
                    padding: '16px',
                    fontFamily: 'Arial, sans-serif',
                    color: '#ffffff',
                    display: 'flex',
                    flexDirection: 'column',
                    overflow: 'auto',
                }}
            >
                {/* Header with refresh icon - cursor remains pointer even during loading */}
                <div style={{ marginBottom: '12px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <IoReload
                            size={20}
                            onClick={refreshItems}
                            style={{
                                cursor: 'pointer',  // Always pointer, even during loading
                                transition: 'transform 0.2s'
                            }}
                            onMouseOver={(e) => (e.target.style.transform = 'scale(1.05)')}
                            onMouseOut={(e) => (e.target.style.transform = 'scale(1)')}
                            title="Refresh"
                        />
                    </div>
                </div>

                {/* File tree */}
                <div style={{ flex: 1, overflowY: 'auto', marginBottom: '12px' }}>
                    {items.length === 0 ? (
                        <p style={{ fontSize: '14px', color: '#cccccc' }}>No files or folders available</p>
                    ) : (
                        <FileTree
                            nodes={items}
                            onToggleFolder={toggleFolder}
                            onFileClick={handleFileClick}
                        />
                    )}
                </div>

                {/* Download button */}
                {items.length > 0 && (
                    <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '30px' }}>
                        <button
                            onClick={downloadAsZip}
                            className='button'
                        >
                            Download ZIP
                        </button>
                    </div>
                )}
            </div>

            {/* File preview modal */}
            <FilePreview
                item={previewItem}
                content={previewContent}
                onClose={closePreview}
            />
        </>
    );
};

export default FileExplorer;