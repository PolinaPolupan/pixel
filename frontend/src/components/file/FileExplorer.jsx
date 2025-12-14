import React, { useState, useEffect } from 'react';
import { IoReload, IoDownload, IoChevronForward, IoHome } from 'react-icons/io5';
import { FileGrid } from './FileGrid.jsx';
import { FilePreview } from './FilePreview.jsx';
import { useFileExplorer } from '../../hooks/useFileExplorer.js';
import './FileExplorer.css';

const FileExplorer = () => {
    const {
        items,
        previewItem,
        previewContent,
        refreshItems,
        handleFileClick,
        closePreview,
        downloadAsZip
    } = useFileExplorer();

    const [currentPath, setCurrentPath] = useState([]);
    const [currentItems, setCurrentItems] = useState([]);

    // Update current items when items or path changes
    useEffect(() => {
        if (currentPath.length === 0) {
            setCurrentItems(items);
        } else {
            let current = items;
            for (const segment of currentPath) {
                const folder = current.find(item => item.path === segment && item.type === 'folder');
                if (folder) {
                    current = [... folder.folders, ...folder.files];
                } else {
                    break;
                }
            }
            setCurrentItems(current);
        }
    }, [items, currentPath]);

    const handleItemClick = (item) => {
        if (item. type === 'folder') {
            setCurrentPath(prev => [...prev, item.path]);
        } else {
            handleFileClick(item);
        }
    };

    const navigateToBreadcrumb = (index) => {
        if (index === -1) {
            setCurrentPath([]);
        } else {
            setCurrentPath(prev => prev.slice(0, index + 1));
        }
    };

    const getBreadcrumbs = () => {
        return currentPath.map(path => {
            const parts = path. split('/');
            return parts[parts.length - 1] || path;
        });
    };

    return (
        <>
            <div className="file-explorer file-explorer-horizontal">
                <div className="file-explorer-toolbar">
                    <div className="file-explorer-breadcrumbs">
                        <button
                            className={`breadcrumb-item ${currentPath.length === 0 ? 'active' : ''}`}
                            onClick={() => navigateToBreadcrumb(-1)}
                            title="Home"
                        >
                            <IoHome size={14} />
                        </button>
                        {getBreadcrumbs().map((crumb, index) => (
                            <React.Fragment key={index}>
                                <IoChevronForward size={12} className="breadcrumb-separator" />
                                <button
                                    className={`breadcrumb-item ${index === currentPath.length - 1 ? 'active' : ''}`}
                                    onClick={() => navigateToBreadcrumb(index)}
                                >
                                    {crumb}
                                </button>
                            </React.Fragment>
                        ))}
                    </div>
                    <div className="file-explorer-toolbar-right">
                        <button
                            onClick={refreshItems}
                            className="file-explorer-toolbar-btn"
                            title="Refresh"
                        >
                            <IoReload size={16} />
                        </button>
                        <button
                            onClick={downloadAsZip}
                            className="file-explorer-toolbar-btn"
                            title="Download ZIP"
                        >
                            <IoDownload size={16} />
                        </button>
                    </div>
                </div>
                <div className="file-explorer-content-horizontal">
                    <FileGrid
                        items={currentItems}
                        onItemClick={handleItemClick}
                    />
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