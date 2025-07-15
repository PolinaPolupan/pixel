// Create this file: utils/api.js
const API_BASE_URL = 'http://localhost:8080/v1';

/**
 * Process a graph for a specific scene
 * @param {number} sceneId - The scene ID
 * @param {object} graphData - The graph data to process
 * @returns {Promise<object>} - The task data
 */
export async function processGraph(sceneId, graphData) {
    const response = await fetch(`${API_BASE_URL}/scene/${sceneId}/graph`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(graphData),
        credentials: 'include'
    });

    if (!response.ok) {
        let errorMessage;
        try {
            const errorData = await response.json();
            errorMessage = errorData.message || errorData.errorMessage || `Server error (${response.status})`;
        } catch (e) {
            const errorText = await response.text();
            errorMessage = errorText || `Server error (${response.status})`;
        }
        throw new Error(errorMessage);
    }

    return response.json();
}

/**
 * Get the status of a task
 * @param {number} sceneId - The scene ID
 * @param {number} taskId - The task ID
 * @returns {Promise<object>} - The task status data
 */
export async function getTaskStatus(sceneId, taskId) {
    const response = await fetch(`${API_BASE_URL}/scene/${sceneId}/task/${taskId}/status`, {
        credentials: 'include'
    });

    if (!response.ok) {
        let errorMessage;
        try {
            const errorData = await response.json();
            errorMessage = errorData.message || errorData.errorMessage || `Server error (${response.status})`;
        } catch (e) {
            const errorText = await response.text();
            errorMessage = errorText || `Server error (${response.status})`;
        }
        throw new Error(errorMessage);
    }

    return response.json();
}