import React from 'react';
import { IoReload } from 'react-icons/io5';
import { FileTree } from './FileTree.jsx';
import { FilePreview } from './FilePreview.jsx';
import { useFileExplorer } from '../../hooks/useFileExplorer.js';

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
                <div style={{ flex: 1, overflowY: 'auto', marginBottom: '12px' }}>
                    <FileTree
                        nodes={items}
                        onToggleFolder={toggleFolder}
                        onFileClick={handleFileClick}
                    />
                </div>
                <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '30px' }}>
                    <button
                        onClick={downloadAsZip}
                        className='button'
                    >
                        Download ZIP
                    </button>
                </div>
            </div>
            <FilePreview
                item={previewItem}
                content={previewContent}
                onClose={closePreview}
            />
        </>
    );
};

export default FileExplorer;