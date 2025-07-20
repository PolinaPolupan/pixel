import { graphApi } from './api.js'

/**
 * A service for managing and monitoring tasks
 */
export class TaskManager {
    constructor() {
        this.pollingIntervals = {};
    }

    /**
     * Monitor a task's progress
     * @param {number} sceneId - The scene ID
     * @param {number} taskId - The task ID
     * @param {object} initialData - Initial task data (if available)
     * @param {function} onProgress - Progress callback
     * @param {function} onComplete - Completion callback
     * @param {function} onError - Error callback
     */
    monitorTask(sceneId, taskId, initialData, onProgress, onComplete, onError) {
        // If we already have task data, check if it's already completed or failed
        if (initialData) {
            console.log(`Initial task status: ${initialData.status}`);

            if (initialData.status === 'COMPLETED') {
                // Simulate some progress first (for better UX)
                if (onProgress) {
                    onProgress({
                        current: Math.floor((initialData.totalNodes || 1) * 0.5),
                        total: initialData.totalNodes || 1
                    });

                    // After a short delay, show completion
                    setTimeout(() => {
                        if (onComplete) onComplete(initialData);
                    }, 500);
                } else {
                    if (onComplete) onComplete(initialData);
                }
                return;
            }
            if (initialData.status === 'FAILED') {
                console.log('Task already failed, calling onError with:', initialData.errorMessage);
                if (onError) {
                    // The error message should be properly formatted for notification
                    const errorMsg = initialData.errorMessage || 'Task failed';
                    console.log('Passing error to notification system:', errorMsg);
                    onError(errorMsg);
                }
                return;
            }
        }

        // Start polling for task status
        this.pollTaskStatus(sceneId, taskId, onProgress, onComplete, onError);
    }

    /**
     * Poll for task status updates
     */
    async pollTaskStatus(sceneId, taskId, onProgress, onComplete, onError) {
        if (this.pollingIntervals[taskId]) {
            clearTimeout(this.pollingIntervals[taskId]);
        }

        try {
            console.log(`Polling task status for task ${taskId}...`);
            // Use graphApi.getTaskStatus instead of getTaskStatus directly
            const taskData = await graphApi.getTaskStatus(sceneId, taskId);

            console.log(`Task ${taskId} status: ${taskData.status}`);

            if (taskData.status === 'PROCESSING') {
                if (onProgress) {
                    onProgress({
                        current: taskData.processedNodes || 0,
                        total: taskData.totalNodes || 1
                    });
                }

                // Continue polling
                this.pollingIntervals[taskId] = setTimeout(() => {
                    this.pollTaskStatus(sceneId, taskId, onProgress, onComplete, onError);
                }, 200);
            }
            else if (taskData.status === 'COMPLETED') {
                // Update to 100% first
                if (onProgress) {
                    onProgress({
                        current: taskData.totalNodes || 1,
                        total: taskData.totalNodes || 1
                    });
                }

                // Then after a short delay mark as complete
                setTimeout(() => {
                    if (onComplete) onComplete(taskData);
                }, 300);

                this.stopPolling(taskId);
            }
            else if (taskData.status === 'FAILED') {
                if (onError) onError(taskData.errorMessage || 'Task failed');
                this.stopPolling(taskId);
            }
            else {
                // Keep polling for other statuses
                this.pollingIntervals[taskId] = setTimeout(() => {
                    this.pollTaskStatus(sceneId, taskId, onProgress, onComplete, onError);
                }, 200);
            }
        } catch (error) {
            console.error(`Error polling task ${taskId}:`, error);
            if (onError) onError(error.message);
            this.stopPolling(taskId);
        }
    }

    /**
     * Stop polling for a specific task
     */
    stopPolling(taskId) {
        if (this.pollingIntervals[taskId]) {
            clearTimeout(this.pollingIntervals[taskId]);
            delete this.pollingIntervals[taskId];
        }
    }

    /**
     * Stop all polling
     */
    stopAllPolling() {
        Object.keys(this.pollingIntervals).forEach(taskId => {
            clearTimeout(this.pollingIntervals[taskId]);
        });
        this.pollingIntervals = {};
    }
}

// Create a singleton instance
export const taskManager = new TaskManager();