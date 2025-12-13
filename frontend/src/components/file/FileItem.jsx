import React from 'react';
import { IoDocumentOutline, IoImage, IoFolderOutline, IoFolderOpenOutline } from 'react-icons/io5';

export const FileItem = ({
                             item,
                             depth,
                             onToggleFolder,
                             onFileClick
                         }) => {
    if (item.type === 'folder') {
        return (
            <div
                className={`file-item file-item-folder ${item.isOpen ?  'open' : ''}`}
                style={{ paddingLeft: `${8 + depth * 20}px` }}
                onClick={(e) => {
                    e.stopPropagation();
                    onToggleFolder(item.path);
                }}
            >
                <span className="file-item-icon">
                    {item.isOpen ? <IoFolderOpenOutline size={20} /> : <IoFolderOutline size={20} />}
                </span>
                <span className="file-item-name">{item. name || 'Unnamed Folder'}</span>
            </div>
        );
    }

    return (
        <div
            className="file-item"
            style={{ paddingLeft: `${8 + depth * 20}px` }}
            onClick={(e) => {
                e.stopPropagation();
                onFileClick(item);
            }}
        >
            <span className="file-item-icon">
                {item.fileType === 'text' ? <IoDocumentOutline size={18} /> : <IoImage size={18} />}
            </span>
            <span className="file-item-name">{item. path.split('/').pop()}</span>
        </div>
    );
};