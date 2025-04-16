import React, { useState, useEffect } from 'react';
import JSZip from 'jszip';
import { saveAs } from 'file-saver';
import { useScene } from './SceneContext';
import { IoReload } from 'react-icons/io5';

const OutputGallery = ({ success, setError }) => {
  const { sceneId } = useScene();
  const [items, setItems] = useState([]);
  const [isOpen, setIsOpen] = useState(false);
  const [previewImage, setPreviewImage] = useState(null);

  const fetchItems = async (baseUrl, basePath = '') => {
    try {
      const response = await fetch(baseUrl);
      if (!response.ok) {
        throw new Error(`Failed to fetch items from ${baseUrl}`);
      }
      const urls = await response.json();
      const newItems = [];

      for (const url of urls) {
        const name = url.split('/').pop();
        const path = basePath ? `${basePath}/${name}` : name;

        if (url.endsWith('/')) {
          newItems.push({
            type: 'folder',
            name,
            path,
            files: [],
            isOpen: false,
          });
        } else if (/\.(png|jpeg|jpg)$/i.test(name)) {
          newItems.push({
            type: 'file',
            url,
            path,
          });
        }
      }

      return newItems;
    } catch (err) {
      setError(err.message);
      return [];
    }
  };

  const fetchAllItems = async () => {
    const baseItems = await fetchItems(`http://localhost:8080/v1/scene/${sceneId}/output`);
    const updatedItems = [];

    for (const item of baseItems) {
      if (item.type === 'folder') {
        const folderFiles = await fetchItems(
          `http://localhost:8080/v1/scene/${sceneId}/output/${item.path}`,
          item.path
        );
        item.files = folderFiles.filter((f) => f.type === 'file');
        updatedItems.push(item);
      } else {
        updatedItems.push(item);
      }
    }

    setItems(updatedItems);
    setIsOpen(true);
  };

  useEffect(() => {
    if (success) {
      fetchAllItems();
    }
  }, [success, sceneId, setError]);

  const toggleFolder = (path) => {
    setItems((prev) =>
      prev.map((item) =>
        item.path === path ? { ...item, isOpen: !item.isOpen } : item
      )
    );
  };

  const downloadAsZip = async () => {
    const zip = new JSZip();
    try {
      const addFilesToZip = async (files, folder = zip) => {
        const promises = files.map(async (file) => {
          if (file.type === 'file') {
            const response = await fetch(file.url);
            if (!response.ok) throw new Error(`Failed to fetch ${file.url}`);
            const blob = await response.blob();
            folder.file(file.path, blob);
          } else if (file.type === 'folder') {
            const subFolder = folder.folder(file.name);
            await addFilesToZip(file.files, subFolder);
          }
        });
        await Promise.all(promises);
      };

      await addFilesToZip(items);
      const zipBlob = await zip.generateAsync({ type: 'blob' });
      saveAs(zipBlob, 'output_images.zip');
    } catch (err) {
      setError('Failed to create ZIP: ' + err.message);
    }
  };

  const handleImageClick = (url) => {
    setPreviewImage(url);
  };

  const handleDownload = (url) => {
    const link = document.createElement('a');
    link.href = url;
    link.download = url.split('/').pop();
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const closePreview = () => {
    setPreviewImage(null);
  };

  return (
    <div
      style={{
        position: 'absolute',
        top: '60px',
        right: '20px',
        zIndex: 10,
      }}
    >
      <button
        onClick={() => setIsOpen(!isOpen)}
        style={{
          background: 'rgba(40, 40, 40, 0.9)',
          border: '1px solid rgba(255, 255, 255, 0.2)',
          borderRadius: '8px',
          padding: '8px 12px',
          color: '#ffffff',
          cursor: 'pointer',
          fontFamily: 'Arial, sans-serif',
          fontSize: '14px',
          boxShadow: '0 2px 4px rgba(0, 0, 0, 0.3)',
          transition: 'background 0.2s, transform 0.1s',
        }}
        onMouseOver={(e) => {
          e.target.style.background = 'rgba(60, 60, 60, 0.9)';
          e.target.style.transform = 'scale(1.05)';
        }}
        onMouseOut={(e) => {
          e.target.style.background = 'rgba(40, 40, 40, 0.9)';
          e.target.style.transform = 'scale(1)';
        }}
      >
        {isOpen ? 'Hide Gallery' : 'Show Gallery'}
      </button>
      {isOpen && (
        <div
          style={{
            marginTop: '10px',
            width: '320px',
            background: 'rgba(40, 40, 40, 0.95)',
            border: '1px solid rgba(255, 255, 255, 0.3)',
            borderRadius: '8px',
            padding: '16px',
            maxHeight: '420px',
            overflowY: 'auto',
            boxShadow: '0 6px 16px rgba(0, 0, 0, 0.5)',
            fontFamily: 'Arial, sans-serif',
            color: '#ffffff',
            zIndex: 20,
          }}
        >
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: '12px',
            }}
          >
            <h3 style={{ margin: '0', fontSize: '16px' }}>Output Files</h3>
            <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
              <IoReload
                onClick={fetchAllItems}
                style={{
                  width: '24px',
                  height: '24px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#ffffff',
                  cursor: 'pointer',
                  fontSize: '16px',
                  transition: 'transform 0.2s',
                }}
                onMouseOver={(e) => {
                  e.target.style.transform = 'scale(1.05)';
                }}
                onMouseOut={(e) => {
                  e.target.style.transform = 'scale(1)';
                }}
                title="Refresh"
              />
              {items.length > 0 && (
                <button
                  onClick={downloadAsZip}
                  style={{
                    background: 'rgb(0, 110, 0)',
                    border: '1px solid rgba(255, 255, 255, 0.2)',
                    borderRadius: '8px',
                    padding: '6px 10px',
                    color: '#ffffff',
                    cursor: 'pointer',
                    fontSize: '12px',
                    transition: 'background 0.2s, transform 0.1s',
                  }}
                  onMouseOver={(e) => {
                    e.target.style.background = 'rgb(0, 200, 0)';
                    e.target.style.transform = 'scale(1.05)';
                  }}
                  onMouseOut={(e) => {
                    e.target.style.background = 'rgb(0, 110, 0)';
                    e.target.style.transform = 'scale(1)';
                  }}
                >
                  Download All as ZIP
                </button>
              )}
            </div>
          </div>
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(2, 1fr)',
              gap: '12px',
            }}
          >
            {items.length === 0 ? (
              <p style={{ fontSize: '14px', color: '#cccccc', gridColumn: 'span 2' }}>
                No files available
              </p>
            ) : (
              items.map((item, index) =>
                item.type === 'folder' ? (
                  <div
                    key={item.path}
                    style={{ gridColumn: 'span 2', cursor: 'pointer' }}
                    onClick={() => toggleFolder(item.path)}
                  >
                    <div
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        padding: '8px',
                        background: 'rgba(60, 60, 60, 0.5)',
                        borderRadius: '4px',
                        marginBottom: '8px',
                      }}
                    >
                      <span style={{ marginRight: '8px' }}>{item.isOpen ? '📂' : '📁'}</span>
                      <span style={{ fontSize: '14px', color: '#ffffff' }}>{item.name}</span>
                    </div>
                    {item.isOpen && item.files.length > 0 && (
                      <div
                        style={{
                          display: 'grid',
                          gridTemplateColumns: 'repeat(2, 1fr)',
                          gap: '12px',
                          marginLeft: '16px',
                        }}
                      >
                        {item.files.map((file, fileIndex) => (
                          <div key={fileIndex} style={{ textAlign: 'center' }}>
                            <img
                              src={file.url}
                              alt={file.path}
                              style={{
                                maxWidth: '100%',
                                maxHeight: '120px',
                                borderRadius: '4px',
                                objectFit: 'contain',
                                background: 'rgba(0, 0, 0, 0.2)',
                                cursor: 'pointer',
                                transition: 'transform 0.2s',
                              }}
                              onClick={() => handleImageClick(file.url)}
                              onMouseOver={(e) => (e.target.style.transform = 'scale(1.05)')}
                              onMouseOut={(e) => (e.target.style.transform = 'scale(1)')}
                            />
                            <p
                              style={{
                                margin: '4px 0 0',
                                fontSize: '12px',
                                color: '#cccccc',
                                wordBreak: 'break-all',
                              }}
                            >
                              {file.path.split('/').pop()}
                            </p>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                ) : (
                  <div key={item.path} style={{ textAlign: 'center' }}>
                    <img
                      src={item.url}
                      alt={item.path}
                      style={{
                        maxWidth: '100%',
                        maxHeight: '120px',
                        borderRadius: '4px',
                        objectFit: 'contain',
                        background: 'rgba(0, 0, 0, 0.2)',
                        cursor: 'pointer',
                        transition: 'transform 0.2s',
                      }}
                      onClick={() => handleImageClick(item.url)}
                      onMouseOver={(e) => (e.target.style.transform = 'scale(1.05)')}
                      onMouseOut={(e) => (e.target.style.transform = 'scale(1)')}
                    />
                    <p
                      style={{
                        margin: '4px 0 0',
                        fontSize: '12px',
                        color: '#cccccc',
                        wordBreak: 'break-all',
                      }}
                    >
                      {item.path.split('/').pop()}
                    </p>
                  </div>
                )
              )
            )}
          </div>
        </div>
      )}
      {previewImage && (
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
            zIndex: 30,
          }}
          onClick={closePreview}
        >
          <div
            style={{
              position: 'relative',
              maxWidth: '90%',
              maxHeight: '90%',
              background: 'rgba(40, 40, 40, 0.95)',
              borderRadius: '8px',
              padding: '16px',
              boxShadow: '0 6px 16px rgba(0, 0, 0, 0.5)',
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <img
              src={previewImage}
              alt="Preview"
              style={{
                maxWidth: '100%',
                maxHeight: '70vh',
                objectFit: 'contain',
                borderRadius: '4px',
              }}
            />
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                marginTop: '12px',
              }}
            >
              <button
                onClick={() => handleDownload(previewImage)}
                style={{
                  background: 'rgb(0, 110, 0)',
                  border: '1px solid rgba(255, 255, 255, 0.2)',
                  borderRadius: '8px',
                  padding: '6px 12px',
                  color: '#ffffff',
                  cursor: 'pointer',
                  fontSize: '14px',
                  transition: 'background 0.2s, transform 0.1s',
                }}
                onMouseOver={(e) => {
                  e.target.style.background = 'rgb(0, 200, 0)';
                  e.target.style.transform = 'scale(1.05)';
                }}
                onMouseOut={(e) => {
                  e.target.style.background = 'rgb(0, 110, 0)';
                  e.target.style.transform = 'scale(1)';
                }}
              >
                Download
              </button>
              <button
                onClick={closePreview}
                style={{
                  background: 'rgba(80, 80, 80, 0.9)',
                  border: '1px solid rgba(255, 255, 255, 0.2)',
                  borderRadius: '8px',
                  padding: '6px 12px',
                  color: '#ffffff',
                  cursor: 'pointer',
                  fontSize: '14px',
                  transition: 'background 0.2s, transform 0.1s',
                }}
                onMouseOver={(e) => {
                  e.target.style.background = 'rgba(100, 100, 100, 0.9)';
                  e.target.style.transform = 'scale(1.05)';
                }}
                onMouseOut={(e) => {
                  e.target.style.background = 'rgba(80, 80, 80, 0.9)';
                  e.target.style.transform = 'scale(1)';
                }}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default OutputGallery;