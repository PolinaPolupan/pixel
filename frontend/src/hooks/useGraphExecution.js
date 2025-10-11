import { useState, useCallback } from 'react';
import { graphApi } from '../services/api.js';
import { useProgress } from "../services/contexts/ProgressContext.jsx";
import { taskManager } from "../services/TaskManager.jsx";
import {useNotification} from "../services/contexts/NotificationContext.jsx";

export function useGraphExecution(getGraphData) {
    const [isProcessing, setIsProcessing] = useState(false);
    const { initProgress, updateProgress, completeProgress, handleError } = useProgress();
    const { setError, setSuccess } = useNotification();

    const executeGraph = useCallback(async () => {
        setError(null);
        setIsProcessing(true);
        console.log("Setting processing state to true");

        initProgress();
        try {
            let graphData = getGraphData();

            graphData.id = 'graph_' + Math.random().toString(36).substring(2, 10);
            graphData.schedule = null;

            console.log("Sending graph data to backend:", graphData);

            await graphApi.create(graphData);
            const taskData = await graphApi.processGraph(graphData.id);
            console.log("Graph processing task created:", taskData);

            taskManager.monitorTask(
                taskData.id,
                taskData,
                (progressData) => updateProgress(progressData),
                (completedData) => {
                    completeProgress(completedData);
                    setSuccess('Graph execution completed successfully');
                    setIsProcessing(false);
                },
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
    }, [initProgress, updateProgress, completeProgress, handleError, setError, setSuccess]);


    return { isProcessing, executeGraph };
}