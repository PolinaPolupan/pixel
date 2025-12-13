import React from 'react';
import { IoClose } from 'react-icons/io5';

export const FilePreview = ({ item, content, onClose }) => {
    if (!item) return null;

    return (
        <div className="file-preview-overlay" onClick={onClose}>
            <div className="file-preview-container" onClick={(e) => e.stopPropagation()}>
                {item.fileType === 'image' ? (
                    <img
                        src={item.url}
                        alt="Preview"
                        className="file-preview-image"
                    />
                ) : (
                    <div className="file-preview-text">
                        {content || 'Loading text content...'}
                    </div>
                )}
                <IoClose
                    onClick={onClose}
                    size={24}
                    className="file-preview-close"
                />
            </div>
        </div>
    );
};