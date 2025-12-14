import {useCallback, useEffect, useRef, useState} from 'react';
import {saveAs} from 'file-saver';
import {graphApi} from '../services/api.js';
import {useNotification} from "../services/contexts/NotificationContext.jsx";

export function useFileExplorer() {
    const { setError } = useNotification();
    const [items, setItems] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [cacheBuster, setCacheBuster] = useState(Date.now());
    const [previewItem, setPreviewItem] = useState(null);
    const [previewContent, setPreviewContent] = useState(null);

    const initialMountRef = useRef(true);

    const fetchItems = useCallback(async () => {
        try {
            setIsLoading(true);
            return await graphApi.listFiles('');
        } catch (err) {
            setError?.(err.message);
            console.error('FileExplorer fetchItems error:', err);
            return [];
        } finally {
            setIsLoading(false);
        }
    }, [setError]);

    const buildTree = useCallback((paths) => {
        const isFile = path => {
            const segments = path.split('/');
            const lastSegment = segments[segments.length - 1];
            return /\.(png|jpeg|jpg|gif|txt|json|md)$/i.test(lastSegment);
        };

        const getFileType = path => {
            if (/\.(png|jpeg|jpg|gif)$/i.test(path)) return 'image';
            if (/\.(txt|json|md)$/i.test(path)) return 'text';
            return 'binary';
        };

        const rootFiles = [];
        const folders = {};

        folders[''] = {
            type: 'folder',
            name: 'root',
            path: '',
            files: [],
            folders:  []
        };

        paths.forEach(path => {
            if (path.includes('/')) {
                const segments = path.split('/');
                let currentPath = '';

                for (let i = 0; i < segments.length - 1; i++) {
                    const segment = segments[i];
                    const parentPath = currentPath;
                    currentPath = currentPath ?  `${currentPath}/${segment}` : segment;

                    if (!folders[currentPath]) {
                        folders[currentPath] = {
                            type: 'folder',
                            name: segment,
                            path: currentPath,
                            files: [],
                            folders: [],
                            parentPath
                        };
                    }
                }

                if (isFile(path)) {
                    const lastSlash = path. lastIndexOf('/');
                    const fileName = path.substring(lastSlash + 1);
                    const folderPath = path. substring(0, lastSlash);

                    if (!folders[folderPath]) {
                        folders[folderPath] = {
                            type: 'folder',
                            name: folderPath.split('/').pop(),
                            path: folderPath,
                            files: [],
                            folders: []
                        };
                    }

                    folders[folderPath].files.push({
                        type: 'file',
                        fileType: getFileType(path),
                        name: fileName,
                        path: path,
                        url: graphApi. getFileUrl(path, cacheBuster)
                    });
                }
            } else if (isFile(path)) {
                rootFiles.push({
                    type: 'file',
                    fileType: getFileType(path),
                    name: path,
                    path: path,
                    url: graphApi.getFileUrl(path, cacheBuster)
                });
            } else {
                if (!folders[path]) {
                    folders[path] = {
                        type:  'folder',
                        name: path,
                        path:  path,
                        files: [],
                        folders: [],
                        parentPath: ''
                    };
                }
            }
        });

        folders[''].files = rootFiles;

        Object.values(folders).forEach(folder => {
            if (folder.path && folder.parentPath !== undefined) {
                const parent = folders[folder.parentPath];
                if (parent && !parent. folders.some(f => f. path === folder.path)) {
                    parent.folders.push(folder);
                }
            }
        });

        return folders[''];
    }, [cacheBuster]);

    const refreshItems = useCallback(async () => {
        const newCacheBuster = Date.now();
        setCacheBuster(newCacheBuster);

        const paths = await fetchItems();
        const root = buildTree(paths);

        setItems([... root.folders, ...root.files]);
        return root;
    }, [fetchItems, buildTree]);

    const downloadAsZip = useCallback(async () => {
        try {
            setIsLoading(true);
            const zipBlob = await graphApi.downloadZip();
            saveAs(zipBlob, 'files.zip');
        } catch (err) {
            console.error('ZIP download error:', err);
            setError?.('Failed to download ZIP: ' + err. message);
        } finally {
            setIsLoading(false);
        }
    }, [setError]);

    const fetchTextContent = useCallback(async (url) => {
        try {
            const response = await fetch(url);
            if (!response. ok) {
                throw new Error(`Failed to fetch text: ${response.statusText}`);
            }
            return await response.text();
        } catch (err) {
            setError?.(`Failed to load text file: ${err.message}`);
            return null;
        }
    }, [setError]);

    const handleFileClick = useCallback(async (item) => {
        setPreviewItem(item);

        if (item. fileType === 'text') {
            const content = await fetchTextContent(item.url);
            setPreviewContent(content);
        } else {
            setPreviewContent(null);
        }
    }, [fetchTextContent]);

    const closePreview = useCallback(() => {
        setPreviewItem(null);
        setPreviewContent(null);
    }, []);

    useEffect(() => {
        if (initialMountRef.current) {
            initialMountRef.current = false;
            refreshItems();
        }
    }, [refreshItems]);

    return {
        items,
        isLoading,
        previewItem,
        previewContent,
        refreshItems,
        handleFileClick,
        closePreview,
        downloadAsZip
    };
}