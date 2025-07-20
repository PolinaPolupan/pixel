// Create this file: contexts/ProgressContext.jsx
import React, { createContext, useContext, useState, useCallback } from 'react';

// Create the context
const ProgressContext = createContext(null);

export function ProgressProvider({ children }) {
    const [progressState, setProgressState] = useState({
        visible: false,
        current: 0,
        total: 1,
        percent: 0,
        fadeOut: false
    });

    // Initialize progress (called when Play button is clicked)
    const initProgress = useCallback(() => {
        console.log('PROGRESS CONTEXT: Initializing progress');

        // Show the progress bar at 0%
        setProgressState({
            visible: true,
            current: 0,
            total: 1,
            percent: 0,
            fadeOut: false
        });
    }, []);

    // Update progress (for incremental updates)
    const updateProgress = useCallback((data) => {
        console.log('PROGRESS CONTEXT: Updating progress', data);

        if (!data || typeof data.current !== 'number' || typeof data.total !== 'number') {
            console.warn('Invalid progress data received:', data);
            return;
        }

        const total = Math.max(data.total, 1); // Ensure we don't divide by zero
        const current = Math.min(data.current, total); // Ensure current doesn't exceed total
        const percent = Math.round((current / total) * 100);

        setProgressState({
            visible: true,
            current: current,
            total: total,
            percent: percent,
            fadeOut: false
        });
    }, []);

    // Complete progress (called when task is completed)
    const completeProgress = useCallback((data) => {
        console.log('PROGRESS CONTEXT: Completing progress with data', data);

        // Get total from data or use 1 as fallback
        const total = data?.totalNodes || 1;

        // Show 100% completion
        setProgressState({
            visible: true,
            current: total,
            total: total,
            percent: 100,
            fadeOut: false
        });

        // Fade out after delay
        setTimeout(() => {
            setProgressState(prev => ({...prev, fadeOut: true}));

            // Hide after fade
            setTimeout(() => {
                setProgressState(prev => ({...prev, visible: false}));
            }, 1000);
        }, 2000);
    }, []);

    // Handle error
    const handleError = useCallback((errorMsg) => {
        console.log('PROGRESS CONTEXT: Handling error', errorMsg);

        // Hide progress bar
        setProgressState(prev => ({...prev, visible: false}));
    }, []);

    // Expose the context
    const contextValue = {
        progressState,
        initProgress,
        updateProgress,
        completeProgress,
        handleError
    };

    return (
        <ProgressContext.Provider value={contextValue}>
            {children}
        </ProgressContext.Provider>
    );
}

// Custom hook to use the progress context
export function useProgress() {
    const context = useContext(ProgressContext);
    if (context === null) {
        throw new Error('useProgress must be used within a ProgressProvider');
    }
    return context;
}