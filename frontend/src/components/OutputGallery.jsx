import React, { useState, useEffect } from 'react';
import JSZip from 'jszip';
import { saveAs } from 'file-saver';
import { useScene } from './SceneContext';
import { IoReload } from 'react-icons/io5';
import { IoClose } from "react-icons/io5";

const FileExplorer = ({ setError }) => {
  const { sceneId } = useScene();
  const [items, setItems] = useState([]);
  const [isOpen, setIsOpen] = useState(false);
  const [previewImage, setPreviewImage] = useState(null);

  const fetchItems = async (folder = '') => {
    try {
      const url = `http://localhost:8080/v1/scene/${sceneId}/output/list${folder ? `?folder=${encodeURIComponent(folder)}` : ''}`;
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(`Failed to fetch items from ${url}`);
      }
      const paths = await response.json();
      return paths.map((path) => path.replace(/\/+$/, '')); // Normalize paths
    } catch (err) {
      setError(err.message);
      return [];
    }
  };

  const buildTree = (paths) => {
    const root = { type: 'folder', name: 'root', path: '', files: [], folders: [], isOpen: true };

    paths.forEach((path) => {
      const segments = path.split('/').filter((s) => s);
      let current = root;

      // Handle folder segments
      segments.forEach((segment, index) => {
        const isLast = index === segments.length - 1;
        const currentPath = segments.slice(0, index + 1).join('/');

        if (isLast && /\.(png|jpeg|jpg)$/i.test(segment)) {
          // File
          current.files.push({
            type: 'file',
            url: `http://localhost:8080/v1/scene/${sceneId}/output/file?filename=${encodeURIComponent(currentPath)}`,
            path: currentPath,
          });
        } else {
          // Folder
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
    });

    return root.folders; // Return top-level folders
  };

  const fetchAllItems = async () => {
    const paths = await fetchItems();
    const tree = buildTree(paths);
    setItems(tree);
    setIsOpen(true);
  };

  useEffect(() => {
    fetchAllItems();
  }, [sceneId, setError]);

  const toggleFolder = async (path) => {
    setItems((prev) => {
      const updateTree = (nodes) =>
        nodes.map((node) => {
          if (node.path !== path) {
            return { ...node, folders: updateTree(node.folders) };
          }
          const newNode = { ...node, isOpen: !node.isOpen };
          if (newNode.isOpen && newNode.files.length === 0 && newNode.folders.length === 0) {
            fetchItems(path).then((subPaths) => {
              const subTree = buildTree(subPaths.filter((p) => p.startsWith(path)));
              setItems((current) => {
                const mergeTree = (nodes) =>
                  nodes.map((n) =>
                    n.path === path
                      ? { ...n, files: subTree.find((t) => t.path === path)?.files || [], folders: subTree.find((t) => t.path === path)?.folders || [], isOpen: true }
                      : { ...n, folders: mergeTree(n.folders) }
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
    const zip = new JSZip();
    try {
      const addFilesToZip = async (nodes, folder = zip) => {
        const promises = nodes.map(async (node) => {
          if (node.type === 'file') {
            const response = await fetch(node.url);
            if (!response.ok) throw new Error(`Failed to fetch ${node.url}`);
            const blob = await response.blob();
            folder.file(node.path, blob);
          } else if (node.type === 'folder') {
            const subFolder = folder.folder(node.name);
            await addFilesToZip(node.files.concat(node.folders), subFolder);
          }
        });
        await Promise.all(promises);
      };

      await addFilesToZip(items);
      const zipBlob = await zip.generateAsync({ type: 'blob' });
      saveAs(zipBlob, 'output_files.zip');
    } catch (err) {
      setError('Failed to create ZIP: ' + err.message);
    }
  };

  const handleImageClick = (url) => {
    setPreviewImage(url);
  };

  const closePreview = () => {
    setPreviewImage(null);
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
              <span style={{ marginRight: '8px' }}>{item.isOpen ? '📂' : '📁'}</span>
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
              onClick={() => handleImageClick(item.url)}
            >
              <span style={{ marginRight: '8px' }}>🖼️</span>
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
        {isOpen ? 'Hide Explorer' : 'Show Explorer'}
      </button>
      {isOpen && (
        <div
          style={{
            marginTop: '10px',
            width: '300px',
            background: 'rgba(40, 40, 40, 0.95)',
            border: '1px solid rgba(255, 255, 255, 0.3)',
            borderRadius: '8px',
            padding: '16px',
            maxHeight: '400px',
            overflowY: 'auto',
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
            <h3 style={{ margin: '0', fontSize: '16px' }}>Files</h3>
            <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
              <IoReload
                onClick={fetchAllItems}
                style={{
                  cursor: 'pointer',
                  fontSize: '16px',
                  transition: 'transform 0.2s',
                }}
                onMouseOver={(e) => (e.target.style.transform = 'scale(1.05)')}
                onMouseOut={(e) => (e.target.style.transform = 'scale(1)')}
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
                  Download ZIP
                </button>
              )}
            </div>
          </div>
          {items.length === 0 ? (
            <p style={{ fontSize: '14px', color: '#cccccc' }}>No files or folders available</p>
          ) : (
            renderTree(items)
          )}
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
            <IoClose 
              onClick={closePreview}
              style={{
                position: 'absolute',
                top: '8px',
                right: '8px',
                background: 'rgba(80, 80, 80, 0.9)',
                border: '1px solid rgba(255, 255, 255, 0.2)',
                borderRadius: '50%',
                width: '24px',
                height: '24px',
                color: '#ffffff',
                cursor: 'pointer',
                fontSize: '14px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
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
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default FileExplorer;