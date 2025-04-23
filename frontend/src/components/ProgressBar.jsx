import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client/dist/sockjs.min.js';
import { Stomp } from '@stomp/stompjs';

export default function ProgressBar({ sceneId, setIsProcessing, setSuccess }) {
  const [progress, setProgress] = useState(null);
  const [isConnected, setIsConnected] = useState(false);
  const stompClientRef = useRef(null);
  const reconnectTimeoutRef = useRef(null);

  // Store latest callback functions in refs to avoid closure issues
  const setIsProcessingRef = useRef(setIsProcessing);
  const setSuccessRef = useRef(setSuccess);

  // Keep refs updated with latest function references
  useEffect(() => {
    setIsProcessingRef.current = setIsProcessing;
    setSuccessRef.current = setSuccess;
  }, [setIsProcessing, setSuccess]);

  useEffect(() => {
    if (!sceneId) return;

    let isMounted = true;
    const timeouts = [];

    const connect = () => {
      try {
        console.log('Connecting to WebSocket...');
        const socket = new SockJS('http://localhost:8080/ws');
        const client = Stomp.over(socket);

        client.debug = process.env.NODE_ENV === 'development' ? console.log : () => {};

        client.connect({}, frame => {
          console.log('Connected to WebSocket:', frame);
          if (!isMounted) return;
          setIsConnected(true);

          client.subscribe(`/topic/processing/${sceneId}`, message => {
            try {
              const data = JSON.parse(message.body);
              console.log('WebSocket message received:', data);

              if (data.status === 'in_progress') {
                if (isMounted) {
                  setProgress({
                    current: data.processedNodes,
                    total: data.totalNodes,
                    percent: data.progressPercent,
                    message: data.message
                  });
                }
              } else if (data.status === 'completed') {
                console.log('************ COMPLETION MESSAGE RECEIVED ************');

                if (!isMounted) {
                  console.log('Component unmounted, skipping completion handling');
                  return;
                }

                setProgress({
                  current: data.processedNodes,
                  total: data.processedNodes,
                  percent: 100,
                  message: data.message
                });

                let completionMessage = data.message || 'Graph processing completed successfully';
                if (data.timestamp && data.processedBy) {
                  completionMessage = `${completionMessage} at ${data.timestamp} by ${data.processedBy}`;
                }

                console.log('Setting isProcessing to false');
                setIsProcessingRef.current(false);
                setSuccessRef.current(completionMessage);

                const fadeTimeout = setTimeout(() => {
                  if (isMounted) {
                    setProgress(prev => (prev ? { ...prev, fadeOut: true } : null));
                  }
                }, 2000);

                const clearTimeout = setTimeout(() => {
                  if (isMounted) {
                    setProgress(null);
                  }
                }, 3000);

                timeouts.push(fadeTimeout, clearTimeout);
              }
            } catch (error) {
              console.error('Error parsing WebSocket message:', error);
            }
          });
        }, error => {
          console.error('WebSocket connection error:', error);
          if (isMounted) {
            setIsConnected(false);
            reconnectTimeoutRef.current = setTimeout(() => {
              console.log('Attempting to reconnect...');
              connect();
            }, 5000);
          }
        });

        stompClientRef.current = client;
      } catch (error) {
        console.error('Error setting up WebSocket:', error);
      }
    };

    connect();

    return () => {
      isMounted = false;
      timeouts.forEach(clearTimeout);
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
      if (stompClientRef.current && stompClientRef.current.connected) {
        stompClientRef.current.disconnect();
      }
      stompClientRef.current = null;
      setIsConnected(false);
    };
  }, [sceneId]);

  // Always render a container with fixed height
  return (
    <div
      style={{
        width: '100%',
        height: '26px', // Fixed height to match progress bar height
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      {progress ? (
        <div
          style={{
            width: '100%',
            backgroundColor: 'rgba(0, 0, 0, 0.1)',
            borderRadius: '4px',
            overflow: 'hidden',
            padding: '4px',
            opacity: progress.fadeOut ? 0 : 1,
            transition: 'opacity 1s ease',
            boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)'
          }}
        >
          <div
            style={{
              height: '8px',
              width: `${progress.percent}%`,
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
            {progress.current} / {progress.total} nodes processed ({progress.percent}%)
          </div>
        </div>
      ) : isConnected ? (
        <div
          style={{
            fontSize: '12px',
            color: '#666',
            textAlign: 'center'
          }}
        >
          Waiting for processing to start...
        </div>
      ) : (
        <div style={{ fontSize: '12px', color: '#666', textAlign: 'center' }}>
          Connecting...
        </div>
      )}
    </div>
  );
}