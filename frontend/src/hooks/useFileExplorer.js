import { useState, useCallback, useEffect, useRef } from 'react';
import { useScene } from '../services/contexts/SceneContext.jsx';
import { saveAs } from 'file-saver';
import { sceneApi } from '../services/api.js';
import {useNotification} from "../services/contexts/NotificationContext.jsx";

/**
 * Custom hook for file explorer operations
 */
export function useFileExplorer() {
    const { setError } = useNotification();
    const { sceneId } = useScene();
    const [items, setItems] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [cacheBuster, setCacheBuster] = useState(Date.now());
    const [previewItem, setPreviewItem] = useState(null);
    const [previewContent, setPreviewContent] = useState(null);

    // Use a ref to track initial mount to prevent duplicate fetching
    const initialMountRef = useRef(true);

    /**
     * Fetch items from the server
     */
    const fetchItems = useCallback(async (folder = '') => {
        if (!sceneId) return [];

        try {
            setIsLoading(true);

            // Use the centralized API client instead of direct fetch
            const paths = await sceneApi.listFiles(sceneId, folder);

            console.log(`FileExplorer fetchItems response (folder=${folder}):`, paths);
            return paths.map((path) => path.replace(/\/+$/, '')); // Normalize paths
        } catch (err) {
            setError?.(err.message);
            console.error(`FileExplorer fetchItems error (folder=${folder}):`, err);
            return [];
        } finally {
            setIsLoading(false);
        }
    }, [sceneId, setError]);

    /**
     * Build a tree structure from paths
     */
    const buildTree = useCallback((paths) => {
        // Helper function to check if a path is a file
        const isFile = path => {
            const segments = path.split('/');
            const lastSegment = segments[segments.length - 1];
            return /\.(png|jpeg|jpg|gif|txt|json|md)$/i.test(lastSegment);
        };

        // Helper function to get file type
        const getFileType = path => {
            if (/\.(png|jpeg|jpg|gif)$/i.test(path)) return 'image';
            if (/\.(txt|json|md)$/i.test(path)) return 'text';
            return 'binary';
        };

        // First, separate folders and root files
        const rootFiles = [];
        const folders = {};

        // Add root folder first
        folders[''] = {
            type: 'folder',
            name: 'root',
            path: '',
            files: [],
            folders: [],
            isOpen: true
        };

        // Create a record of all folders and their parent paths
        paths.forEach(path => {
            if (path.includes('/')) {
                // This is a nested path, get all folder segments
                const segments = path.split('/');
                let currentPath = '';

                // Process each segment to build folder hierarchy
                for (let i = 0; i < segments.length - 1; i++) {
                    const segment = segments[i];
                    const parentPath = currentPath;

                    // Build the current path
                    currentPath = currentPath ? `${currentPath}/${segment}` : segment;

                    // Create folder if it doesn't exist
                    if (!folders[currentPath]) {
                        folders[currentPath] = {
                            type: 'folder',
                            name: segment,
                            path: currentPath,
                            files: [],
                            folders: [],
                            isOpen: false,
                            parentPath
                        };
                    }
                }

                // If it's a file, add it to its parent folder
                if (isFile(path)) {
                    const lastSlash = path.lastIndexOf('/');
                    const fileName = path.substring(lastSlash + 1);
                    const folderPath = path.substring(0, lastSlash);

                    // Make sure the parent folder exists
                    if (!folders[folderPath]) {
                        folders[folderPath] = {
                            type: 'folder',
                            name: folderPath.split('/').pop(),
                            path: folderPath,
                            files: [],
                            folders: [],
                            isOpen: false
                        };
                    }

                    // Add file to its parent folder
                    folders[folderPath].files.push({
                        type: 'file',
                        fileType: getFileType(path),
                        name: fileName,
                        path: path,
                        url: sceneApi.getFileUrl(sceneId, path, cacheBuster)
                    });
                }
            } else if (isFile(path)) {
                // This is a root-level file
                rootFiles.push({
                    type: 'file',
                    fileType: getFileType(path),
                    name: path,
                    path: path,
                    url: sceneApi.getFileUrl(sceneId, path, cacheBuster)
                });
            } else {
                // This is a root-level folder
                if (!folders[path]) {
                    folders[path] = {
                        type: 'folder',
                        name: path,
                        path: path,
                        files: [],
                        folders: [],
                        isOpen: false,
                        parentPath: ''
                    };
                }
            }
        });

        // Add all files to the root folder
        folders[''].files = rootFiles;

        // Now connect all folders to their parents
        Object.values(folders).forEach(folder => {
            if (folder.path && folder.parentPath !== undefined) {
                const parent = folders[folder.parentPath];
                if (parent && !parent.folders.some(f => f.path === folder.path)) {
                    parent.folders.push(folder);
                }
            }
        });

        // The root of our tree is the '' folder
        return folders[''];
    }, [sceneId, cacheBuster]);

    /**
     * Refresh all items
     */
    const refreshItems = useCallback(async () => {
        const newCacheBuster = Date.now();
        setCacheBuster(newCacheBuster);

        const paths = await fetchItems('');
        const root = buildTree(paths);

        // Set the children of the root as our top-level items
        setItems([...root.folders, ...root.files]);

        return root;
    }, [fetchItems, buildTree]);

    /**
     * Toggle folder open/closed state
     */
    const toggleFolder = useCallback((path) => {
        setItems(prev => {
            // Create a deep copy to avoid mutation
            const updateNode = (nodes) => {
                return nodes.map(node => {
                    if (node.path === path) {
                        return { ...node, isOpen: !node.isOpen };
                    }

                    if (node.type === 'folder') {
                        return {
                            ...node,
                            folders: updateNode(node.folders),
                            files: [...node.files]
                        };
                    }

                    return node;
                });
            };

            return updateNode(prev);
        });
    }, []);

    /**
     * Download all files as a ZIP
     */
    const downloadAsZip = useCallback(async () => {
        try {
            setIsLoading(true);

            // Use the API method but with the working implementation
            const zipBlob = await sceneApi.downloadZip(sceneId);

            // Use the exact save logic from the working version
            saveAs(zipBlob, `scene_${sceneId}_files.zip`);
        } catch (err) {
            console.error('ZIP download error:', err);
            setError?.('Failed to download ZIP: ' + err.message);
        } finally {
            setIsLoading(false);
        }
    }, [sceneId, setError]);

    /**
     * Fetch text content for a file
     */
    const fetchTextContent = useCallback(async (url) => {
        try {
            const response = await fetch(url);
            if (!response.ok) {
                throw new Error(`Failed to fetch text: ${response.statusText}`);
            }
            return await response.text();
        } catch (err) {
            setError?.(`Failed to load text file: ${err.message}`);
            return null;
        }
    }, [setError]);

    /**
     * Handle file click - opens preview
     */
    const handleFileClick = useCallback(async (item) => {
        setPreviewItem(item);

        if (item.fileType === 'text') {
            const content = await fetchTextContent(item.url);
            setPreviewContent(content);
        } else {
            setPreviewContent(null);
        }
    }, [fetchTextContent]);

    /**
     * Close file preview
     */
    const closePreview = useCallback(() => {
        setPreviewItem(null);
        setPreviewContent(null);
    }, []);

    // Initial load - using useEffect with a ref to prevent infinite loops
    useEffect(() => {
        if (sceneId && initialMountRef.current) {
            initialMountRef.current = false;
            refreshItems();
        }
    }, [sceneId]); // Remove refreshItems from dependencies

    // Expose a manual refresh function that won't trigger re-renders
    const manualRefresh = useCallback(() => {
        refreshItems();
    }, [refreshItems]);

    return {
        items,
        isLoading,
        previewItem,
        previewContent,
        refreshItems: manualRefresh, // Use manualRefresh instead of refreshItems
        toggleFolder,
        handleFileClick,
        closePreview,
        downloadAsZip
    };
}