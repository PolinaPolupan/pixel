import React, { useState, useEffect, useRef } from 'react';

export default function ProgressBar({setIsProcessing, setSuccess, setError}) {
  // Refs to store the latest function references
  const setIsProcessingRef = useRef(setIsProcessing);
  const setSuccessRef = useRef(setSuccess);
  const setErrorRef = useRef(setError);

  // State for the progress bar
  const [progressState, setProgressState] = useState({
    visible: false,
    current: 0,
    total: 1,
    percent: 0,
    fadeOut: false
  });

  // Update refs when props change
  useEffect(() => {
    setIsProcessingRef.current = setIsProcessing;
    setSuccessRef.current = setSuccess;
    setErrorRef.current = setError;
  }, [setIsProcessing, setSuccess, setError]);

  // Store timeouts to clear them when unmounting
  const timeoutsRef = useRef([]);

  // Clear all timeouts on unmount
  useEffect(() => {
    return () => {
      timeoutsRef.current.forEach(id => clearTimeout(id));
    };
  }, []);

  // Initialize progress (called when Play button is clicked)
  const initProgress = () => {
    console.log('PROGRESS BAR: Initializing progress');

    // Clear existing timeouts
    timeoutsRef.current.forEach(id => clearTimeout(id));
    timeoutsRef.current = [];

    // Show the progress bar at 0%
    setProgressState({
      visible: true,
      current: 0,
      total: 1,
      percent: 0,
      fadeOut: false
    });
  };

  // Update progress (for incremental updates)
  const updateProgress = (data) => {
    console.log('PROGRESS BAR: Updating progress', data);

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
  };

  // Complete progress (called when task is completed)
  const completeProgress = (data) => {
    console.log('PROGRESS BAR: Completing progress with data', data);

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

    // Set success message
    setSuccessRef.current('Graph processing completed successfully');

    // Set isProcessing to false
    setIsProcessingRef.current(false);

    // Fade out after delay
    const fadeTimeout = setTimeout(() => {
      setProgressState(prev => ({...prev, fadeOut: true}));
    }, 2000);

    // Hide after fade
    const hideTimeout = setTimeout(() => {
      setProgressState(prev => ({...prev, visible: false}));
    }, 3000);

    timeoutsRef.current.push(fadeTimeout, hideTimeout);
  };

  // Handle error
  const handleError = (errorMsg) => {
    console.log('PROGRESS BAR: Handling error', errorMsg);

    // Hide progress bar
    setProgressState(prev => ({...prev, visible: false}));

    // Show error
    setErrorRef.current(errorMsg || 'Processing failed');

    // Set isProcessing to false
    setIsProcessingRef.current(false);
  };

  // Expose functions via window object
  useEffect(() => {
    window.progressFunctions = {
      init: initProgress,
      update: updateProgress,
      complete: completeProgress,
      error: handleError
    };

    return () => {
      delete window.progressFunctions;
    };
  }, []);

  // Render the progress bar
  return (
      <div
          style={{
            width: '100%',
            height: '26px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
      >
        {progressState.visible ? (
            <div
                style={{
                  width: '100%',
                  backgroundColor: 'rgba(0, 0, 0, 0.1)',
                  borderRadius: '4px',
                  overflow: 'hidden',
                  padding: '4px',
                  opacity: progressState.fadeOut ? 0 : 1,
                  transition: 'opacity 1s ease',
                  boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)'
                }}
            >
              <div
                  style={{
                    height: '8px',
                    width: `${progressState.percent}%`,
                    backgroundColor: '#4caf50',
                    borderRadius: '4px',
                    transition: 'width 0.3s ease',
                  }}
              />
              <div
                  style={{
                    fontSize: '12px',
                    color: '#333',
                    marginTop: '4px',
                    textAlign: 'center'
                  }}
              >
                {progressState.current} / {progressState.total} nodes processed ({progressState.percent}%)
              </div>
            </div>
        ) : (
            <div style={{ fontSize: '12px', color: '#666', textAlign: 'center' }}>
              Ready for processing
            </div>
        )}
      </div>
  );
}