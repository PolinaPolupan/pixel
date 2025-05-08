import React, { useState, useEffect } from 'react';
import { saveAs } from 'file-saver';
import { useScene } from './SceneContext';
import {IoReload, IoClose, IoDocumentOutline} from 'react-icons/io5';
import { IoFolderOutline } from "react-icons/io5";
import { IoFolderOpenOutline } from "react-icons/io5";
import { IoImage } from "react-icons/io5";

const FileExplorer = ({ setError }) => {
  const { sceneId } = useScene();
  const [items, setItems] = useState([]);
  const [isOpen, setIsOpen] = useState(true);
  const [previewItem, setPreviewItem] = useState(null);
  const [previewContent, setPreviewContent] = useState(null);
  const [cacheBuster, setCacheBuster] = useState(Date.now());

  const fetchItems = async (folder = '') => {
    try {
      const url = `http://localhost:8080/v1/scene/${sceneId}/list${folder ? `?folder=${encodeURIComponent(folder)}` : ''}`;
      const response = await fetch(url, {
        credentials: 'include',
        headers: {
          'Cache-Control': 'no-cache, no-store, must-revalidate',
          'Pragma': 'no-cache',
          'Expires': '0',
        },
      });
      if (!response.ok) {
        throw new Error(`Failed to fetch items from ${url}: ${response.statusText}`);
      }
      const paths = await response.json();
      console.log(`FileExplorer fetchItems response (folder=${folder}):`, paths);
      return paths.map((path) => path.replace(/\/+$/, '')); // Normalize paths
    } catch (err) {
      setError(err.message);
      console.error(`FileExplorer fetchItems error (folder=${folder}):`, err);
      return [];
    }
  };

  const buildTree = (paths) => {
    const root = { type: 'folder', name: 'files', path: '', files: [], folders: [], isOpen: true };

    paths.forEach((path) => {
      const segments = path.split('/').filter((s) => s);
      let current = root;

      if (segments.length === 1 && /\.(png|jpeg|jpg|txt)$/i.test(segments[0])) {
        const fileType = /\.txt$/i.test(segments[0]) ? 'text' : 'image';
        current.files.push({
          type: 'file',
          fileType: fileType,
          url: `http://localhost:8080/v1/scene/${sceneId}/file?filepath=${encodeURIComponent(segments[0])}&_cb=${cacheBuster}`,
          path: segments[0],
        });
      } else {
        segments.forEach((segment, index) => {
          const isLast = index === segments.length - 1;
          const currentPath = segments.slice(0, index + 1).join('/');

          if (isLast && /\.(png|jpeg|jpg|txt)$/i.test(segment)) {
            const fileType = /\.txt$/i.test(segment) ? 'text' : 'image';
            current.files.push({
              type: 'file',
              fileType: fileType,
              url: `http://localhost:8080/v1/scene/${sceneId}/file?filepath=${encodeURIComponent(currentPath)}&_cb=${cacheBuster}`,
              path: currentPath,
            });
          } else {
            let folder = current.folders.find((f) => f.name === segment);
            if (!folder) {
              folder = {
                type: 'folder',
                name: segment,
                path: currentPath,
                files: [],
                folders: [],
                isOpen: false,
              };
              current.folders.push(folder);
            }
            current = folder;
          }
        });
      }
    });

    console.log('FileExplorer buildTree result:', { folders: root.folders, files: root.files });
    return root;
  };

  const fetchTextContent = async (url) => {
    try {
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(`Failed to fetch text: ${response.statusText}`);
      }
      return await response.text();
    } catch (err) {
      setError(`Failed to load text file: ${err.message}`);
      return null;
    }
  };

  const fetchAllItems = async () => {
    setCacheBuster(Date.now());
    const paths = await fetchItems('');
    const fileTree = buildTree(paths);
    console.log('FileExplorer fetchAllItems tree:', fileTree);
    setItems([fileTree]);
  };

  useEffect(() => {
    fetchAllItems();
  }, [sceneId, setError]);

  const toggleFolder = async (path) => {
    setItems((prev) => {
      const updateTree = (nodes) =>
          nodes.map((node) => {
            if (node.path !== path || node.type !== 'folder') {
              return { ...node, folders: node.folders ? updateTree(node.folders) : [] };
            }
            const newNode = { ...node, isOpen: !node.isOpen };
            if (newNode.isOpen && newNode.files.length === 0 && newNode.folders.length === 0) {
              fetchItems(path).then((subPaths) => {
                const subTree = buildTree(subPaths);
                setItems((current) => {
                  const mergeTree = (nodes) =>
                      nodes.map((n) =>
                          n.path === path
                              ? { ...n, files: subTree.files || [], folders: subTree.folders || [], isOpen: true }
                              : { ...n, folders: n.folders ? mergeTree(n.folders) : [] }
                      );
                  return mergeTree(current);
                });
              });
            }
            return newNode;
          });
      return updateTree(prev);
    });
  };

  const downloadAsZip = async () => {
    try {
      const zipUrl = `http://localhost:8080/v1/scene/${sceneId}/zip`;

      const response = await fetch(zipUrl);

      if (!response.ok) {
        throw new Error(`Server returned ${response.status}: ${response.statusText}`);
      }

      const zipBlob = await response.blob();

      saveAs(zipBlob, `scene_${sceneId}_files.zip`);

    } catch (err) {
      setError('Failed to download ZIP: ' + err.message);
    }
  };

  const handleFileClick = async (item) => {
    setPreviewItem(item);

    if (item.fileType === 'text') {
      const content = await fetchTextContent(item.url);
      setPreviewContent(content);
    }
  };

  const closePreview = () => {
    setPreviewItem(null);
    setPreviewContent(null);
  };


  const renderTree = (nodes, depth = 0) => (
      <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
        {nodes.map((item) => (
            <li key={item.path}>
              {item.type === 'folder' ? (
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
                      onClick={() => toggleFolder(item.path)}
                  >
                    <span style={{ marginRight: '8px' }}>{item.isOpen ? <IoFolderOpenOutline size={20}/> : <IoFolderOutline size={20}/>}</span>
                    <span style={{ fontSize: '14px' }}>{item.name || 'Unnamed Folder'}</span>
                  </div>
              ) : (
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
                      onClick={() => handleFileClick(item)}
                  >
              <span style={{ marginRight: '8px' }}>
                {item.fileType === 'text' ? <IoDocumentOutline /> : <IoImage />}
              </span>
                    <span style={{ fontSize: '14px' }}>{item.path.split('/').pop()}</span>
                  </div>
              )}
              {item.type === 'folder' && item.isOpen && (item.files.length > 0 || item.folders.length > 0) && (
                  <div style={{ margin: '8px 0' }}>{renderTree(item.folders.concat(item.files), depth + 1)}</div>
              )}
            </li>
        ))}
      </ul>
  );

  return (
      <>
        <div
            style={{
              width: '100%',
              height: '100%',
              padding: '16px',
              fontFamily: 'Arial, sans-serif',
              color: '#ffffff',
              display: 'flex',
              flexDirection: 'column',
              overflow: 'auto',
            }}
        >
          {/* Header with File Explorer label and refresh icon */}
          <div style={{ marginBottom: '12px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <IoReload
                  size={20}
                  onClick={fetchAllItems}
                  style={{
                    cursor: 'pointer',
                    transition: 'transform 0.2s',
                  }}
                  onMouseOver={(e) => (e.target.style.transform = 'scale(1.05)')}
                  onMouseOut={(e) => (e.target.style.transform = 'scale(1)')}
                  title="Refresh"
              />
            </div>
          </div>

          {/* File tree */}
          {isOpen ? (
              items.length === 0 ? (
                  <p style={{ fontSize: '14px', color: '#cccccc' }}>No files or folders available</p>
              ) : (
                  <div style={{ flex: 1, overflowY: 'auto', marginBottom: '12px' }}>
                    {renderTree(items)}
                  </div>
              )
          ) : (
              <p style={{ fontSize: '14px', color: '#cccccc' }}>Explorer hidden</p>
          )}

          {/* Download button */}
          {items.length > 0 && (
              <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '30px' }}>
                <button
                    onClick={downloadAsZip}
                    className='button'
                >
                  Download ZIP
                </button>
              </div>
          )}
        </div>

        {/* Image preview modal */}
        {previewItem && (
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
                onClick={closePreview}
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
                {previewItem.fileType === 'image' ? (
                    <img
                        src={previewItem.url}
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
                      {previewContent || 'Loading text content...'}
                    </div>
                )}
                <IoClose
                    onClick={closePreview}
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
        )}
      </>
  );
};

export default FileExplorer;