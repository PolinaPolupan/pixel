import React from 'react';
import { FileItem } from './FileItem';

export const FileTree = ({ nodes, depth = 0, onToggleFolder, onFileClick }) => {
    return (
        <ul className="file-tree">
            {nodes.map((item) => (
                <li key={item.path} className="file-tree-item">
                    <FileItem
                        item={item}
                        depth={depth}
                        onToggleFolder={onToggleFolder}
                        onFileClick={onFileClick}
                    />

                    {item.type === 'folder' && item.isOpen && (item.files.length > 0 || item.folders.length > 0) && (
                        <div className="file-tree-children">
                            <FileTree
                                nodes={[...item.folders, ...item.files]}
                                depth={depth + 1}
                                onToggleFolder={onToggleFolder}
                                onFileClick={onFileClick}
                            />
                        </div>
                    )}
                </li>
            ))}
        </ul>
    );
};