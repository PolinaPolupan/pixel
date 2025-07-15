import { useState, useCallback } from 'react';
import { processGraph } from '../utils/api';
import { taskManager } from '../components/services/TaskManager';
import { useProgress } from '../components/contexts/ProgressContext';

export function useGraphExecution({ sceneId, transformGraphData, setError, setSuccess }) {
    const [isProcessing, setIsProcessing] = useState(false);
    const { initProgress, updateProgress, completeProgress, handleError } = useProgress();

    const executeGraph = useCallback(async () => {
        setError(null);
        setIsProcessing(true);
        console.log("Setting processing state to true");

        // Initialize progress bar
        initProgress();

        try {
            const graphData = transformGraphData();

            console.log("Sending graph data to backend:", graphData);
            const taskData = await processGraph(sceneId, graphData);
            console.log("Graph processing task created:", taskData);

            // Start monitoring progress using the TaskManager
            taskManager.monitorTask(
                sceneId,
                taskData.id,
                taskData,
                // Progress callback
                (progressData) => {
                    updateProgress(progressData);
                },
                // Complete callback
                (completedData) => {
                    completeProgress(completedData);
                    setSuccess('Graph processing completed successfully');
                    setIsProcessing(false);
                },
                // Error callback
                (errorMessage) => {
                    handleError(errorMessage);
                    setError(errorMessage);
                    setIsProcessing(false);
                }
            );
        } catch (error) {
            console.error('Error during graph processing:', error);
            handleError(error.message);
            setError(error.message);
            setIsProcessing(false);
        }
    }, [sceneId, transformGraphData, initProgress, updateProgress, completeProgress, handleError, setError, setSuccess]);

    return {
        isProcessing,
        executeGraph
    };
}