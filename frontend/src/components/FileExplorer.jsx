import React, { useState, useEffect } from 'react';
import JSZip from 'jszip';
import { saveAs } from 'file-saver';
import { useScene } from './SceneContext';
import { IoReload, IoClose } from 'react-icons/io5';
import { IoFolderOutline } from "react-icons/io5";
import { IoFolderOpenOutline } from "react-icons/io5";
import { IoImage } from "react-icons/io5";

const FileExplorer = ({ setError }) => {
  const { sceneId } = useScene();
  const [items, setItems] = useState([]);
  const [isOpen, setIsOpen] = useState(true); // Open by default in dock
  const [previewImage, setPreviewImage] = useState(null);
  const [cacheBuster, setCacheBuster] = useState(Date.now());

  const fetchItems = async (directoryType, folder = '') => {
    try {
      const url = `http://localhost:8080/v1/scene/${sceneId}/${directoryType}/list${folder ? `?folder=${encodeURIComponent(folder)}` : ''}`;
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
      console.log(`FileExplorer fetchItems response (${directoryType}, ${folder}):`, paths);
      return paths.map((path) => path.replace(/\/+$/, '')); // Normalize paths
    } catch (err) {
      setError(err.message);
      console.error(`FileExplorer fetchItems error (${directoryType}, ${folder}):`, err);
      return [];
    }
  };

  const buildTree = (paths, directoryType) => {
    const root = { type: 'folder', name: directoryType, path: directoryType, files: [], folders: [], isOpen: true };

    paths.forEach((path) => {
      const segments = path.split('/').filter((s) => s);
      let current = root;

      if (segments.length === 1 && /\.(png|jpeg|jpg)$/i.test(segments[0])) {
        current.files.push({
          type: 'file',
          url: `http://localhost:8080/v1/scene/${sceneId}/${directoryType}/file?filepath=${encodeURIComponent(path)}&_cb=${cacheBuster}`,
          path: `${directoryType}/${path}`,
        });
      } else {
        segments.forEach((segment, index) => {
          const isLast = index === segments.length - 1;
          const currentPath = `${directoryType}/${segments.slice(0, index + 1).join('/')}`;

          if (isLast && /\.(png|jpeg|jpg)$/i.test(segment)) {
            current.files.push({
              type: 'file',
              url: `http://localhost:8080/v1/scene/${sceneId}/${directoryType}/file?filepath=${encodeURIComponent(path)}&_cb=${cacheBuster}`,
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

    console.log(`FileExplorer buildTree result (${directoryType}):`, { folders: root.folders, files: root.files });
    return root;
  };

  const fetchAllItems = async () => {
    setCacheBuster(Date.now());
    const inputPaths = await fetchItems('input');
    const outputPaths = await fetchItems('output');
    const inputTree = buildTree(inputPaths, 'input');
    const outputTree = buildTree(outputPaths, 'output');
    const tree = [inputTree, outputTree];
    console.log('FileExplorer fetchAllItems tree:', tree);
    setItems(tree);
  };

  useEffect(() => {
    fetchAllItems();
  }, [sceneId, setError]);

  const toggleFolder = async (path, directoryType) => {
    setItems((prev) => {
      const updateTree = (nodes) =>
        nodes.map((node) => {
          if (node.path !== path || node.type !== 'folder') {
            return { ...node, folders: node.folders ? updateTree(node.folders) : [] };
          }
          const newNode = { ...node, isOpen: !node.isOpen };
          if (newNode.isOpen && newNode.files.length === 0 && newNode.folders.length === 0) {
            fetchItems(directoryType, path.replace(`${directoryType}/`, '')).then((subPaths) => {
              const subTree = buildTree(subPaths.filter((p) => p.startsWith(path.replace(`${directoryType}/`, ''))), directoryType);
              setItems((current) => {
                const mergeTree = (nodes) =>
                  nodes.map((n) =>
                    n.path === path
                      ? { ...n, files: subTree.files || [], folders: subTree.folders || [], isOpen: true }
                      : { ...n, folders: mergeTree(n.folders || []) }
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

  const downloadAsZip = async (directoryType = null) => {
    const zip = new JSZip();
    try {
      const addFilesToZip = async (nodes, folder = zip) => {
        const promises = nodes.map(async (node) => {
          if (node.type === 'file') {
            const response = await fetch(node.url);
            if (!response.ok) throw new Error(`Failed to fetch ${node.url}`);
            const blob = await response.blob();
            folder.file(node.path.split('/').pop(), blob);
          } else if (node.type === 'folder') {
            const subFolder = folder.folder(node.name);
            await addFilesToZip(node.files.concat(node.folders), subFolder);
          }
        });
        await Promise.all(promises);
      };

      if (directoryType) {
        const targetItems = items.find((item) => item.name === directoryType);
        if (targetItems) {
          await addFilesToZip(targetItems.files.concat(targetItems.folders));
        }
      } else {
        await addFilesToZip(items);
      }

      const zipBlob = await zip.generateAsync({ type: 'blob' });
      saveAs(zipBlob, directoryType ? `${directoryType}_files.zip` : 'all_files.zip');
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
              onClick={() => toggleFolder(item.path, item.name)}
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
              onClick={() => handleImageClick(item.url)}
            >
              <span style={{ marginRight: '8px' }}><IoImage /></span>
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

        {/* Download buttons at the bottom */}
        {items.length > 0 && (
          <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '30px' }}>
            <button
              onClick={() => downloadAsZip('input')}
              className='button'
            >
              Download Input ZIP
            </button>
            <button
              onClick={() => downloadAsZip('output')}
              className='button'
            >
              Download Output ZIP
            </button>
            <button
              onClick={() => downloadAsZip()}
              className='button'
            >
              Download All ZIP
            </button>
          </div>
        )}
      </div>

      {/* Image preview modal */}
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
            zIndex: 100,
          }}
          onClick={closePreview}
        >
          <div
            style={{
              position: 'relative',
              maxWidth: '90%',
              maxHeight: '90%',
              background: 'rgba(40, 40, 40, 0)',
              padding: '32px',
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <img
              src={previewImage}
              alt="Preview"
              style={{
                maxWidth: '100%',
                maxHeight: '70vh',
                objectFit: 'contain'
              }}
            />
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