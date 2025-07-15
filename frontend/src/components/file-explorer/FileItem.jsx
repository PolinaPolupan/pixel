import React from 'react';
import { IoDocumentOutline, IoImage, IoFolderOutline, IoFolderOpenOutline } from 'react-icons/io5';

/**
 * Renders a file or folder item in the file explorer
 */
export const FileItem = ({
                             item,
                             depth,
                             onToggleFolder,
                             onFileClick
                         }) => {
    if (item.type === 'folder') {
        return (
            <div
                style={{
                    display: 'flex',
                    alignItems: 'center',
                    padding: '8px',
                    paddingLeft: `${8 + depth * 20}px`,
                    cursor: 'pointer',
                    background: item.isOpen ? 'rgba(60, 60, 60, 0.5)' : 'transparent',
                    borderRadius: '4px',
                    marginBottom: '4px',
                }}
                onClick={(e) => {
                    e.stopPropagation(); // Prevent event bubbling
                    onToggleFolder(item.path);
                }}
            >
        <span style={{ marginRight: '8px' }}>
          {item.isOpen ? <IoFolderOpenOutline size={20}/> : <IoFolderOutline size={20}/>}
        </span>
                <span style={{ fontSize: '14px' }}>{item.name || 'Unnamed Folder'}</span>
            </div>
        );
    }

    return (
        <div
            style={{
                display: 'flex',
                alignItems: 'center',
                padding: '8px',
                paddingLeft: `${8 + depth * 20}px`,
                cursor: 'pointer',
                borderRadius: '4px',
                marginBottom: '4px',
            }}
            onClick={(e) => {
                e.stopPropagation(); // Prevent event bubbling
                onFileClick(item);
            }}
        >
      <span style={{ marginRight: '8px' }}>
        {item.fileType === 'text' ? <IoDocumentOutline /> : <IoImage />}
      </span>
            <span style={{ fontSize: '14px' }}>{item.path.split('/').pop()}</span>
        </div>
    );
};