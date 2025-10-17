import React from 'react';
import { IoClose } from 'react-icons/io5';


export const FilePreview = ({ item, content, onClose }) => {
    if (!item) return null;

    return (
        <div
            style={{
                position: 'fixed',
                top: 0,
                left: 0,
                width: '100vw',
                height: '100vh',
                background: 'rgba(0, 0, 0, 0.8)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                zIndex: 100,
            }}
            onClick={onClose}
        >
            <div
                style={{
                    position: 'relative',
                    maxWidth: '90%',
                    maxHeight: '90%',
                    background: 'rgba(40, 40, 40, 0.9)',
                    padding: '32px',
                    borderRadius: '8px',
                }}
                onClick={(e) => e.stopPropagation()}
            >
                {item.fileType === 'image' ? (
                    <img
                        src={item.url}
                        alt="Preview"
                        style={{
                            maxWidth: '100%',
                            maxHeight: '70vh',
                            objectFit: 'contain'
                        }}
                    />
                ) : (
                    <div
                        style={{
                            backgroundColor: '#1e1e1e',
                            padding: '20px',
                            borderRadius: '4px',
                            maxWidth: '800px',
                            maxHeight: '70vh',
                            overflowY: 'auto',
                            fontFamily: 'monospace',
                            whiteSpace: 'pre-wrap',
                            wordBreak: 'break-all',
                            color: '#f8f8f2'
                        }}
                    >
                        {content || 'Loading text content...'}
                    </div>
                )}
                <IoClose
                    onClick={onClose}
                    size={40}
                    style={{
                        position: 'absolute',
                        top: '0px',
                        right: '0px',
                        cursor: 'pointer',
                        fontSize: '14px',
                        display: 'flex'
                    }}
                />
            </div>
        </div>
    );
};