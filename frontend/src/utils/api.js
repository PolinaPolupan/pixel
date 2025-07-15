/**
 * Centralized API client for backend communication
 */
const API_BASE_URL = 'http://localhost:8080/v1';

/**
 * Make an API request with error handling
 */
async function apiRequest(endpoint, options = {}) {
    try {
        // Add credentials to all requests
        const requestOptions = {
            ...options,
            credentials: 'include',
            headers: {
                ...options.headers,
            },
        };

        // Only set Content-Type for JSON requests, not for multipart/form-data (file uploads)
        if (!options.body || !(options.body instanceof FormData)) {
            requestOptions.headers['Content-Type'] = 'application/json';
        }

        const response = await fetch(`${API_BASE_URL}${endpoint}`, requestOptions);

        // Check for error responses
        if (!response.ok) {
            let errorMessage;

            try {
                // Try to parse error as JSON
                const errorData = await response.json();
                errorMessage = errorData.message || errorData.errorMessage || `Server error: ${response.status}`;
            } catch (e) {
                // If not JSON, use text
                const errorText = await response.text();
                errorMessage = errorText || `Server error: ${response.status}`;
            }

            throw new Error(errorMessage);
        }

        // For JSON responses
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        }

        // For non-JSON responses
        return await response.text();
    } catch (error) {
        console.error(`API Error (${endpoint}):`, error);
        throw error;
    }
}

/**
 * Scene API functions
 */
export const sceneApi = {
    /**
     * List files in a scene
     */
    listFiles: (sceneId) =>
        apiRequest(`/scene/${sceneId}/list`),

    /**
     * Get file URL from a scene
     */
    getFileUrl: (sceneId, filePath) =>
        `${API_BASE_URL}/scene/${sceneId}/file?filepath=${encodeURIComponent(filePath)}`,

    /**
     * Upload input file to a scene
     */
    uploadInput: (sceneId, files) => {
        const formData = new FormData();
        for (let i = 0; i < files.length; i++) {
            formData.append('files', files[i]);
        }

        return apiRequest(`/scene/${sceneId}/input`, {
            method: 'POST',
            body: formData
        });
    }
};

/**
 * Graph processing API
 */
export const graphApi = {
    /**
     * Process a graph for a scene
     */
    processGraph: (sceneId, graphData) =>
        apiRequest(`/scene/${sceneId}/graph`, {
            method: 'POST',
            body: JSON.stringify(graphData)
        }),

    /**
     * Get task status
     */
    getTaskStatus: (sceneId, taskId) =>
        apiRequest(`/scene/${sceneId}/task/${taskId}/status`)
};

/**
 * Node configuration API
 */
export const nodeApi = {
    /**
     * Get all node types
     */
    getNodeTypes: () => apiRequest(`/nodes/types`),

    /**
     * Get node configuration
     */
    getNodeConfig: (nodeType) => apiRequest(`/nodes/config/${nodeType}`)
};