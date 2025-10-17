import React, { createContext, useContext, useState, useCallback } from 'react';

const ProgressContext = createContext(null);

export function ProgressProvider({ children }) {
    const [progressState, setProgressState] = useState({
        visible: false,
        current: 0,
        total: 1,
        percent: 0,
        fadeOut: false
    });

    const initProgress = useCallback(() => {
        console.log('PROGRESS CONTEXT: Initializing progress');

        setProgressState({
            visible: true,
            current: 0,
            total: 1,
            percent: 0,
            fadeOut: false
        });
    }, []);

    const updateProgress = useCallback((data) => {
        console.log('PROGRESS CONTEXT: Updating progress', data);

        if (!data || typeof data.current !== 'number' || typeof data.total !== 'number') {
            console.warn('Invalid progress data received:', data);
            return;
        }

        const total = Math.max(data.total, 1);
        const current = Math.min(data.current, total);
        const percent = Math.round((current / total) * 100);

        setProgressState({
            visible: true,
            current: current,
            total: total,
            percent: percent,
            fadeOut: false
        });
    }, []);

    const completeProgress = useCallback((data) => {
        console.log('PROGRESS CONTEXT: Completing progress with data', data);

        const total = data?.totalNodes || 1;

        setProgressState({
            visible: true,
            current: total,
            total: total,
            percent: 100,
            fadeOut: false
        });

        setTimeout(() => {
            setProgressState(prev => ({...prev, fadeOut: true}));

            setTimeout(() => {
                setProgressState(prev => ({...prev, visible: false}));
            }, 1000);
        }, 2000);
    }, []);

    const handleError = useCallback((errorMsg) => {
        console.log('PROGRESS CONTEXT: Handling error', errorMsg);

        setProgressState(prev => ({...prev, visible: false}));
    }, []);

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

export function useProgress() {
    const context = useContext(ProgressContext);
    if (context === null) {
        throw new Error('useProgress must be used within a ProgressProvider');
    }
    return context;
}