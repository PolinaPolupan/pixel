import { useState, useCallback, useEffect } from 'react';
import { useScene } from '../components/contexts/SceneContext.jsx';
import { saveAs } from 'file-saver';

/**
 * Custom hook for file explorer operations
 */
export function useFileExplorer(setError) {
    const { sceneId } = useScene();
    const [items, setItems] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [cacheBuster, setCacheBuster] = useState(Date.now());
    const [previewItem, setPreviewItem] = useState(null);
    const [previewContent, setPreviewContent] = useState(null);

    /**
     * Fetch items from the server
     */
    const fetchItems = useCallback(async (folder = '') => {
        if (!sceneId) return [];

        try {
            setIsLoading(true);
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
        const root = { type: 'folder', name: 'files', path: '', files: [], folders: [], isOpen: true };

        // First, create a set of all directory paths to avoid duplicates
        const dirPaths = new Set();

        // Process all paths to extract directories
        paths.forEach(path => {
            const segments = path.split('/').filter(s => s);
            if (segments.length > 1) {
                // Add all parent directories to the set
                for (let i = 1; i < segments.length; i++) {
                    const dirPath = segments.slice(0, i).join('/');
                    dirPaths.add(dirPath);
                }
            }
        });

        // Now process paths to build the tree
        paths.forEach((path) => {
            const segments = path.split('/').filter((s) => s);
            let current = root;

            // If it's a root-level file
            if (segments.length === 1 && !dirPaths.has(segments[0])) {
                const fileName = segments[0];
                if (/\.(png|jpeg|jpg|txt)$/i.test(fileName)) {
                    const fileType = /\.txt$/i.test(fileName) ? 'text' : 'image';
                    current.files.push({
                        type: 'file',
                        fileType: fileType,
                        url: `http://localhost:8080/v1/scene/${sceneId}/file?filepath=${encodeURIComponent(fileName)}&_cb=${cacheBuster}`,
                        path: fileName,
                    });
                }
            } else {
                // Handle nested paths
                for (let i = 0; i < segments.length; i++) {
                    const segment = segments[i];
                    const isLast = i === segments.length - 1;
                    const currentPath = segments.slice(0, i + 1).join('/');

                    // If this is a directory path or not the last segment
                    if (dirPaths.has(currentPath) || !isLast) {
                        // Find or create folder
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
                    // If it's the last segment and not a directory, it's a file
                    else if (isLast && !dirPaths.has(currentPath)) {
                        if (/\.(png|jpeg|jpg|txt)$/i.test(segment)) {
                            const fileType = /\.txt$/i.test(segment) ? 'text' : 'image';
                            current.files.push({
                                type: 'file',
                                fileType: fileType,
                                url: `http://localhost:8080/v1/scene/${sceneId}/file?filepath=${encodeURIComponent(currentPath)}&_cb=${cacheBuster}`,
                                path: currentPath,
                            });
                        }
                    }
                }
            }
        });

        console.log('FileExplorer buildTree result:', { folders: root.folders, files: root.files });
        return root;
    }, [sceneId, cacheBuster]);

    /**
     * Refresh all items
     */
    const refreshItems = useCallback(async () => {
        setCacheBuster(Date.now());
        const paths = await fetchItems('');
        const fileTree = buildTree(paths);
        setItems([fileTree]);
        return fileTree;
    }, [fetchItems, buildTree]);


    const toggleFolder = (path) => {
        setItems(prev => {
            return updateTreeNode(prev, path, node => ({
                ...node,
                isOpen: !node.isOpen
            }));
        });
    };

    const updateTreeNode = (tree, path, updateFn) => {
        return tree.map(node => {
            if (node.path === path) {
                return updateFn(node);
            }

            if (node.folders) {
                return {
                    ...node,
                    folders: updateTreeNode(node.folders, path, updateFn)
                };
            }

            return node;
        });
    };

    /**
     * Download all files as a ZIP
     */
    const downloadAsZip = useCallback(async () => {
        try {
            const zipUrl = `http://localhost:8080/v1/scene/${sceneId}/zip`;
            const response = await fetch(zipUrl);

            if (!response.ok) {
                throw new Error(`Server returned ${response.status}: ${response.statusText}`);
            }

            const zipBlob = await response.blob();
            saveAs(zipBlob, `scene_${sceneId}_files.zip`);
        } catch (err) {
            setError?.('Failed to download ZIP: ' + err.message);
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
        }
    }, [fetchTextContent]);

    /**
     * Close file preview
     */
    const closePreview = useCallback(() => {
        setPreviewItem(null);
        setPreviewContent(null);
    }, []);

    // Initial load
    useEffect(() => {
        if (sceneId) {
            refreshItems();
        }
    }, [sceneId, refreshItems]);

    return {
        items,
        isLoading,
        previewItem,
        previewContent,
        refreshItems,
        toggleFolder,
        handleFileClick,
        closePreview,
        downloadAsZip
    };
}