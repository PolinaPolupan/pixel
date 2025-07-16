/**
 * Centralized API client for MyPixel
 *
 * This module provides a unified interface for all backend API calls,
 * with consistent error handling, request formatting, and response parsing.
 */

// Base configuration
const API_CONFIG = {
    BASE_URL: 'http://localhost:8080/v1',
    DEFAULT_HEADERS: {
        'Accept': 'application/json'
    },
    CREDENTIALS: 'include'
};

/**
 * Core API request function with standardized error handling
 * @param {string} endpoint - API endpoint path (without base URL)
 * @param {Object} options - Fetch options
 * @returns {Promise<any>} - Parsed response data
 */
async function apiRequest(endpoint, options = {}) {
    const url = `${API_CONFIG.BASE_URL}${endpoint}`;

    try {
        // Prepare request options with defaults
        const requestOptions = {
            ...options,
            credentials: API_CONFIG.CREDENTIALS,
            headers: {
                ...API_CONFIG.DEFAULT_HEADERS,
                ...options.headers,
            },
        };

        // Only set Content-Type for JSON requests, not for multipart/form-data (file uploads)
        if (!options.body || !(options.body instanceof FormData)) {
            requestOptions.headers['Content-Type'] = 'application/json';
        }

        // Execute request
        const response = await fetch(url, requestOptions);

        // Handle error responses
        if (!response.ok) {
            let errorMessage;

            try {
                // Try to parse error as JSON
                const errorData = await response.json();
                errorMessage = errorData.message || errorData.error || `Server error: ${response.status}`;
            } catch (e) {
                // If not JSON, use text
                const errorText = await response.text();
                errorMessage = errorText || `Server error: ${response.status}`;
            }

            throw new Error(errorMessage);
        }

        // Handle empty responses
        if (response.status === 204) {
            return null;
        }

        // Parse response based on content type
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        } else if (contentType && contentType.includes('application/octet-stream')) {
            return await response.blob();
        } else {
            return await response.text();
        }
    } catch (error) {
        // Log error for debugging
        console.error(`API Error (${endpoint}):`, error);

        // Rethrow with additional context if needed
        throw error;
    }
}

/**
 * Scene management API
 */
export const sceneApi = {
    /**
     * Create a new scene
     * @returns {Promise<Object>} New scene data with ID
     */
    create: () =>
        apiRequest('/scene/', { method: 'POST' }),

    /**
     * Get scene details
     * @param {string} sceneId - Scene identifier
     * @returns {Promise<Object>} Scene data
     */
    get: (sceneId) =>
        apiRequest(`/scene/${sceneId}`),

    /**
     * Delete a scene
     * @param {string} sceneId - Scene identifier
     * @returns {Promise<void>}
     */
    delete: (sceneId) =>
        apiRequest(`/scene/${sceneId}`, { method: 'DELETE' }),

    /**
     * List files in a scene
     * @param {string} sceneId - Scene identifier
     * @param {string} [folder] - Optional folder path
     * @returns {Promise<string[]>} List of file paths
     */
    listFiles: (sceneId, folder = '') =>
        apiRequest(`/scene/${sceneId}/list${folder ? `?folder=${encodeURIComponent(folder)}` : ''}`),

    /**
     * Get file URL for a scene file
     * @param {string} sceneId - Scene identifier
     * @param {string} filePath - Path to file
     * @param {number} [cacheBuster] - Optional cache-busting parameter
     * @returns {string} Full URL to file
     */
    getFileUrl: (sceneId, filePath, cacheBuster = Date.now()) =>
        `${API_CONFIG.BASE_URL}/scene/${sceneId}/file?filepath=${encodeURIComponent(filePath)}${cacheBuster ? `&_cb=${cacheBuster}` : ''}`,

    /**
     * Upload input file(s) to a scene
     * @param {string} sceneId - Scene identifier
     * @param {FileList|File[]} files - Files to upload
     * @returns {Promise<string[]>} List of uploaded file paths
     */
    uploadInput: (sceneId, files) => {
        const formData = new FormData();
        if (files instanceof FileList) {
            for (let i = 0; i < files.length; i++) {
                formData.append('file', files[i]);
            }
        } else if (Array.isArray(files)) {
            files.forEach(file => formData.append('file', file));
        } else {
            formData.append('file', files);
        }

        return apiRequest(`/scene/${sceneId}/input`, {
            method: 'POST',
            body: formData
        });
    },

    /**
     * Download scene as ZIP
     * @param {string} sceneId - Scene identifier
     * @returns {Promise<Blob>} ZIP file as blob
     */
    downloadZip: async (sceneId) => {
        // Use the exact working implementation
        const zipUrl = `http://localhost:8080/v1/scene/${sceneId}/zip`;
        const response = await fetch(zipUrl);

        if (!response.ok) {
            throw new Error(`Server returned ${response.status}: ${response.statusText}`);
        }

        return await response.blob();
    }
};

/**
 * Graph processing API
 */
export const graphApi = {
    /**
     * Process a graph for a scene
     * @param {string} sceneId - Scene identifier
     * @param {Object} graphData - Graph data to process
     * @returns {Promise<Object>} Task data
     */
    processGraph: (sceneId, graphData) =>
        apiRequest(`/scene/${sceneId}/graph`, {
            method: 'POST',
            body: JSON.stringify(graphData)
        }),

    /**
     * Get task status
     * @param {string} sceneId - Scene identifier
     * @param {string} taskId - Task identifier
     * @returns {Promise<Object>} Task status data
     */
    getTaskStatus: (sceneId, taskId) =>
        apiRequest(`/scene/${sceneId}/task/${taskId}/status`)
};

/**
 * Node configuration API
 */
export const nodeApi = {
    /**
     * Get all node configurations
     * @returns {Promise<Object>} Map of node configurations
     */
    getNodeConfig: () =>
        apiRequest(`/node/config`),

    /**
     * Get configuration for a specific node type
     * @param {string} nodeType - Node type identifier
     * @returns {Promise<Object>} Node configuration
     */
    getNodeTypeConfig: (nodeType) =>
        apiRequest(`/node/config/${nodeType}`)
};

/**
 * General utility API functions
 */
export const utilApi = {
    /**
     * Check server health
     * @returns {Promise<Object>} Health status
     */
    health: () =>
        apiRequest('/health'),

    /**
     * Get server version information
     * @returns {Promise<Object>} Version data
     */
    version: () =>
        apiRequest('/version')
};