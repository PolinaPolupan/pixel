import React from 'react';
import { IoReload } from 'react-icons/io5';
import { FileTree } from './FileTree.jsx';
import { FilePreview } from './FilePreview.jsx';
import { useFileExplorer } from '../../hooks/useFileExplorer.js';
import './FileExplorer.css';

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
            <div className="file-explorer">
                <div className="file-explorer-header">
                    <div className="file-explorer-actions">
                        <IoReload
                            size={20}
                            onClick={refreshItems}
                            className="file-explorer-refresh"
                            title="Refresh"
                        />
                    </div>
                </div>
                <div className="file-explorer-tree">
                    <FileTree
                        nodes={items}
                        onToggleFolder={toggleFolder}
                        onFileClick={handleFileClick}
                    />
                </div>
                <div className="file-explorer-buttons">
                    <button
                        onClick={downloadAsZip}
                        className="button"
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