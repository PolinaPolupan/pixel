const ENGINE_CONFIG = {
    BASE_URL: 'http://localhost:8080/v1',
    DEFAULT_HEADERS: {
        'Accept': 'application/json'
    },
    CREDENTIALS: 'include'
};

const NODE_CONFIG = {
    BASE_URL: 'http://localhost:8000',
    DEFAULT_HEADERS: {
        'Accept': 'application/json'
    },
    CREDENTIALS: 'include'
};

/**
 * Core API request function with standardized error handling
 * @param {string} endpoint - API endpoint path (may be full URL for external services)
 * @param {Object} options - Fetch options
 * @param {Object} config - Service config (ENGINE_CONFIG or NODE_CONFIG)
 * @returns {Promise<any>} - Parsed response data
 */
async function apiRequest(endpoint, options = {}, config = ENGINE_CONFIG) {
    // Support absolute URLs for external endpoints
    const url = endpoint.startsWith('http') ? endpoint : `${config.BASE_URL}${endpoint}`;

    try {
        // Prepare request options with defaults
        const requestOptions = {
            ...options,
            credentials: config.CREDENTIALS,
            headers: {
                ...config.DEFAULT_HEADERS,
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
 * Engine (scene/graph) API
 */
export const sceneApi = {
    create: () =>
        apiRequest('/scene/', { method: 'POST' }),

    delete: (sceneId) =>
        apiRequest(`/scene/${sceneId}`, { method: 'DELETE' }),

    getFileUrl: (sceneId, filePath, cacheBuster = Date.now()) =>
        `${ENGINE_CONFIG.BASE_URL}/scene/${sceneId}/file?filepath=${encodeURIComponent(filePath)}${cacheBuster ? `&_cb=${cacheBuster}` : ''}`,

    uploadInput: async (sceneId, files) => {
        const formData = new FormData();
        for (const file of files) {
            formData.append('file', file);
        }

        const response = await fetch(`${ENGINE_CONFIG.BASE_URL}/scene/${sceneId}/upload`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to upload files');
        }

        return await response.json();
    },

    listFiles: async (sceneId, folder = '') => {
        const response = await fetch(`${ENGINE_CONFIG.BASE_URL}/scene/${sceneId}/list?folder=${encodeURIComponent(folder)}`);

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to list files');
        }

        return await response.json();
    },

    downloadZip: async (sceneId) => {
        const zipUrl = `${ENGINE_CONFIG.BASE_URL}/scene/${sceneId}/zip`;
        const response = await fetch(zipUrl);

        if (!response.ok) {
            throw new Error(`Server returned ${response.status}: ${response.statusText}`);
        }

        return await response.blob();
    }
};

export const graphApi = {
    processGraph: (sceneId, graphData) =>
        apiRequest(`/scene/${sceneId}/exec`, {
            method: 'POST',
            body: JSON.stringify(graphData)
        }),

    getTaskStatus: (sceneId, taskId) =>
        apiRequest(`/scene/${sceneId}/task/${taskId}/status`)
};

/**
 * Node service API
 */
export const nodeApi = {
    getNodeConfig: () =>
        apiRequest('/info', {}, NODE_CONFIG),
};