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
    CREDENTIALS: 'omit'
};

async function apiRequest(endpoint, options = {}, config = ENGINE_CONFIG) {
    const url = `${config.BASE_URL}${endpoint}`;

    try {
        const requestOptions = {
            ...options,
            credentials: config.CREDENTIALS,
            headers: {
                ...config.DEFAULT_HEADERS,
                ...options.headers,
            },
        };

        if (!options.body || !(options.body instanceof FormData)) {
            requestOptions.headers['Content-Type'] = 'application/json';
        }

        const response = await fetch(url, requestOptions);

        if (!response.ok) {
            let errorMessage;

            try {
                const errorData = await response.json();
                errorMessage = errorData.message || errorData.error || `Server error: ${response.status}`;
            } catch (e) {
                const errorText = await response.text();
                errorMessage = errorText || `Server error: ${response.status}`;
            }

            throw new Error(errorMessage);
        }

        if (response.status === 204) {
            return null;
        }

        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        } else if (contentType && contentType.includes('application/octet-stream')) {
            return await response.blob();
        } else {
            return await response.text();
        }
    } catch (error) {
        console.error(`API Error (${endpoint}):`, error);
        throw error;
    }
}

export const graphApi = {
    uploadInput: async (files) => {
        const formData = new FormData();
        for (const file of files) {
            formData.append('file', file);
        }

        const response = await fetch(`${ENGINE_CONFIG.BASE_URL}/storage/upload`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Failed to upload files');
        }

        return await response.json();
    },

    listFiles: async (folder = '', graphExecutionId = null, nodeId = null) => {
        let endpoint = `/storage/list?folder=${encodeURIComponent(folder)}`;

        if (graphExecutionId !== null && nodeId !== null) {
            endpoint += `&graphExecutionId=${graphExecutionId}&nodeId=${nodeId}`;
        }

        return apiRequest(endpoint);
    },

    getFileUrl: (path, cacheBuster, graphExecutionId = null, nodeId = null) => {
        let url = `${ENGINE_CONFIG.BASE_URL}/storage/files?filepath=${encodeURIComponent(path)}&t=${cacheBuster}`;

        if (graphExecutionId !== null && nodeId !== null) {
            url += `&graphExecutionId=${graphExecutionId}&nodeId=${nodeId}`;
        }

        return url;
    },

    downloadZip: async (folder = '', graphExecutionId = null, nodeId = null) => {
        let endpoint = `/storage/zip?folder=${encodeURIComponent(folder)}`;

        if (graphExecutionId !== null && nodeId !== null) {
            endpoint += `&graphExecutionId=${graphExecutionId}&nodeId=${nodeId}`;
        }

        const response = await fetch(`${ENGINE_CONFIG.BASE_URL}${endpoint}`, {
            credentials: ENGINE_CONFIG.CREDENTIALS,
        });

        if (!response.ok) {
            throw new Error('Failed to download ZIP');
        }

        return await response. blob();
    },

    processGraph: (graphId) =>
        apiRequest(`/graph/${graphId}`, {method: 'POST'}),

    create: (graphData) =>
        apiRequest(`/graph`, {
            method: 'POST',
            body: JSON.stringify(graphData)
        }),

    getTaskStatus: (taskId) =>
        apiRequest(`/task/${taskId}/status`)
};

export const nodeApi = {
    getNodeConfig: () =>
        apiRequest('/info', {}, NODE_CONFIG),
};

export const executionApi = {
    getAll: async (graphId = null) => {
        const endpoint = graphId
            ?  `/graph_execution?graphId=${encodeURIComponent(graphId)}`
            : '/graph_execution';
        return apiRequest(endpoint);
    },

    getById: async (id) => {
        return apiRequest(`/graph_execution/${id}`);
    },

    getNodeExecutions:  async (graphExecutionId) => {
        return apiRequest(`/node_execution?graphExecutionId=${graphExecutionId}`);
    },
}
