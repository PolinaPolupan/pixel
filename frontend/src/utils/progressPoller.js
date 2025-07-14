// utils/progressPoller.js
export function startProgressPolling(sceneId, taskId, initialData) {
    let isPolling = true;
    const pollInterval = 200; // Poll every 200ms for more responsive updates

    // If the task is already completed in the initial response, handle it directly
    if (initialData) {
        console.log("Task already has status:", initialData.status);

        if (initialData.status === "COMPLETED") {
            if (window.progressFunctions) {
                // First show some progress (so it doesn't jump from 0 to 100% instantly)
                window.progressFunctions.update({
                    current: Math.floor((initialData.totalNodes || 1) * 0.5),
                    total: initialData.totalNodes || 1
                });

                // After a short delay, show completion
                setTimeout(() => {
                    window.progressFunctions.complete(initialData);
                }, 500);
            }
            return () => {}; // No-op cleanup function
        } else if (initialData.status === "FAILED") { // Changed from "ERROR" to "FAILED"
            // Handle initial error status
            if (window.progressFunctions) {
                // Show the error message from the task data
                window.progressFunctions.error(initialData.errorMessage || "Task failed");
            }
            return () => {}; // No-op cleanup function
        }
    }

    const poll = async () => {
        if (!isPolling) return;

        try {
            console.log(`Polling task status for task ${taskId}...`);
            const response = await fetch(`http://localhost:8080/v1/scene/${sceneId}/task/${taskId}/status`, {
                credentials: 'include'
            });

            // Handle HTTP error responses
            if (!response.ok) {
                const errorBody = await response.text();
                let errorMessage;

                try {
                    // Try to parse as JSON to extract error message
                    const errorJson = JSON.parse(errorBody);
                    errorMessage = errorJson.message || errorJson.errorMessage || `Server error: ${response.status}`;
                } catch (e) {
                    // If not JSON, use text
                    errorMessage = errorBody || `Server error: ${response.status}`;
                }

                throw new Error(errorMessage);
            }

            const progressData = await response.json();
            console.log("Poll response:", progressData);

            if (progressData.status === 'PROCESSING' && window.progressFunctions) {
                window.progressFunctions.update({
                    current: progressData.processedNodes || 0,
                    total: progressData.totalNodes || 1
                });

                // Continue polling
                setTimeout(poll, pollInterval);
            } else if (progressData.status === 'COMPLETED' && window.progressFunctions) {
                // First update to the final progress state
                window.progressFunctions.update({
                    current: progressData.totalNodes || 1,
                    total: progressData.totalNodes || 1
                });

                // Then after a short delay mark as complete
                setTimeout(() => {
                    window.progressFunctions.complete(progressData);
                }, 300);

                isPolling = false;
            } else if (progressData.status === 'FAILED' && window.progressFunctions) { // Changed from "ERROR" to "FAILED"
                // Handle error status with the provided error message
                window.progressFunctions.error(progressData.errorMessage || "Task failed");
                isPolling = false;
            } else {
                // Keep polling for other statuses (QUEUED, etc.)
                setTimeout(poll, pollInterval);
            }
        } catch (error) {
            console.error("Error polling progress:", error);
            if (window.progressFunctions) {
                window.progressFunctions.error(error.message);
            }
            isPolling = false;
        }
    };

    // Start polling for non-completed or non-failed tasks
    poll();

    // Return function to stop polling
    return () => {
        isPolling = false;
    };
}