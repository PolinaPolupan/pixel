/**
 * Centralized file service for handling file operations
 */
const API_BASE_URL = 'http://localhost:8080/v1';

/**
 * Fetch files and folders from a scene
 * @param {string} sceneId - The scene ID
 * @returns {Promise<Array>} - Array of files and folders
 */
export async function fetchItems(sceneId) {
    try {
        // Use the correct endpoint format: /scene/:sceneId/list
        const response = await fetch(`${API_BASE_URL}/scene/${sceneId}/list`, {
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error(`Failed to fetch items: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Error fetching items:', error);
        throw error;
    }
}

/**
 * Upload files to a scene
 * @param {number} sceneId - The scene ID
 * @param {FileList} files - The files to upload
 * @returns {Promise<Array>} - Response from the server
 */
export async function uploadFiles(sceneId, files) {
    try {
        const formData = new FormData();

        // Add all files to form data
        for (let i = 0; i < files.length; i++) {
            formData.append('files', files[i]);
        }

        const response = await fetch(`${API_BASE_URL}/scene/${sceneId}/input`, {
            method: 'POST',
            body: formData,
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error(`Upload failed: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Error uploading files:', error);
        throw error;
    }
}

/**
 * Get a file URL from the scene
 * @param {number} sceneId - The scene ID
 * @param {string} filePath - Path of the file
 * @returns {string} - URL to the file
 */
export function getFileUrl(sceneId, filePath) {
    return `${API_BASE_URL}/scene/${sceneId}/file?filepath=${encodeURIComponent(filePath)}`;
}

/**
 * Download a file from the scene
 * @param {number} sceneId - The scene ID
 * @param {string} filePath - Path of the file to download
 */
export function downloadFile(sceneId, filePath) {
    const downloadUrl = getFileUrl(sceneId, filePath);

    // Create a temporary link and trigger download
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = filePath.split('/').pop(); // Extract filename
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}