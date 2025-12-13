import React from 'react';
import { IoDocumentOutline, IoImage, IoFolderOutline } from 'react-icons/io5';

export const FileGrid = ({ items, onItemClick }) => {
    const getFileIcon = (item) => {
        if (item.type === 'folder') {
            return <IoFolderOutline size={24} />;
        }
        if (item.fileType === 'image') {
            return <IoImage size={24} />;
        }
        return <IoDocumentOutline size={24} />;
    };

    const getFileName = (item) => {
        if (item.type === 'folder') {
            return item. name || 'Unnamed Folder';
        }
        const parts = item.path.split('/');
        return parts[parts.length - 1];
    };

    if (items.length === 0) {
        return (
            <div className="file-grid-empty">
                No files or folders
            </div>
        );
    }

    return (
        <div className="file-grid">
            {items.map((item) => (
                <div
                    key={item.path}
                    className={`file-grid-item ${item.type === 'folder' ? 'folder' : 'file'}`}
                    onClick={() => onItemClick(item)}
                    title={getFileName(item)}
                >
                    <div className="file-grid-item-icon">
                        {getFileIcon(item)}
                    </div>
                    <div className="file-grid-item-name">
                        {getFileName(item)}
                    </div>
                </div>
            ))}
        </div>
    );
};