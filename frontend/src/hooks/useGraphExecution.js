import { useState, useCallback } from 'react';
import { graphApi } from '../services/api.js';
import { useProgress } from "../services/contexts/ProgressContext.jsx";
import { taskManager } from "../services/TaskManager.jsx";
import {useNotification} from "../services/contexts/NotificationContext.jsx";

export function useGraphExecution({ sceneId, transformGraphData }) {
    const [isProcessing, setIsProcessing] = useState(false);
    const { initProgress, updateProgress, completeProgress, handleError } = useProgress();
    // Use notification context directly
    const { setError, setSuccess } = useNotification();

    const executeGraph = useCallback(async () => {
        if (!sceneId) {
            setError('No active scene');
            return;
        }

        // Clear previous errors
        setError(null);
        setIsProcessing(true);
        console.log("Setting processing state to true");

        // Initialize progress bar
        initProgress();

        try {
            // Transform the graph
            const graphData = transformGraphData();
            console.log("Sending graph data to backend:", graphData);

            // Execute the graph
            const taskData = await graphApi.processGraph(sceneId, graphData);
            console.log("Graph processing task created:", taskData);

            // Monitor the task - NOTE THE CHANGE HERE: taskData.id instead of response.taskId
            taskManager.monitorTask(
                sceneId,
                taskData.id, // THIS WAS THE MAIN ISSUE - using taskData.id not response.taskId
                taskData,
                // Progress callback
                (progressData) => {
                    updateProgress(progressData);
                },
                // Complete callback
                (completedData) => {
                    completeProgress(completedData);
                    setSuccess('Graph execution completed successfully');
                    setIsProcessing(false);
                },
                // Error callback
                (errorMessage) => {
                    handleError(errorMessage);
                    setError(errorMessage);
                    setIsProcessing(false);
                }
            );
        } catch (err) {
            console.error('Error during graph processing:', err);
            handleError(err.message);
            setError(`Failed to execute graph: ${err.message}`);
            setIsProcessing(false);
        }
    }, [sceneId, transformGraphData, initProgress, updateProgress, completeProgress, handleError, setError, setSuccess]);

    return { isProcessing, executeGraph };
}