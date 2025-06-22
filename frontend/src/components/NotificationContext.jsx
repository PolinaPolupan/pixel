import React, { createContext, useState, useEffect, useContext } from 'react';
import { Panel } from '@xyflow/react';
import { NotificationPanel, NotificationKeyframes } from './NotificationPanel';

// Create context
const NotificationContext = createContext();

// Notification types
export const NOTIFICATION_TYPES = {
  ERROR: 'error',
  SUCCESS: 'success',
  WARNING: 'warning',
  INFO: 'info'
};

export function NotificationProvider({ children }) {
  const [notifications, setNotifications] = useState({
    error: null,
    success: null,
    warning: null,
    info: null
  });

  // Clear notification after timeout
  useEffect(() => {
    const timers = {};
    
    Object.entries(notifications).forEach(([type, message]) => {
      if (message) {
        // Default timeout of 5000ms, can be customized per type if needed
        const timeout = type === NOTIFICATION_TYPES.ERROR ? 8000 : 5000;
        
        timers[type] = setTimeout(() => {
          setNotifications(prev => ({ ...prev, [type]: null }));
        }, timeout);
      }
    });
    
    // Clear timers on unmount
    return () => {
      Object.values(timers).forEach(timer => clearTimeout(timer));
    };
  }, [notifications]);

  // Set notification functions
  const setNotification = (type, message) => {
    setNotifications(prev => ({ ...prev, [type]: message }));
  };

  // Clear notification functions
  const clearNotification = (type) => {
    setNotifications(prev => ({ ...prev, [type]: null }));
  };

  // Create convenience functions for each notification type
  const setError = (message) => setNotification(NOTIFICATION_TYPES.ERROR, message);
  const setSuccess = (message) => setNotification(NOTIFICATION_TYPES.SUCCESS, message);
  const setWarning = (message) => setNotification(NOTIFICATION_TYPES.WARNING, message);
  const setInfo = (message) => setNotification(NOTIFICATION_TYPES.INFO, message);

  const clearError = () => clearNotification(NOTIFICATION_TYPES.ERROR);
  const clearSuccess = () => clearNotification(NOTIFICATION_TYPES.SUCCESS);
  const clearWarning = () => clearNotification(NOTIFICATION_TYPES.WARNING);
  const clearInfo = () => clearNotification(NOTIFICATION_TYPES.INFO);

  // Context value
  const value = {
    notifications,
    setNotification,
    clearNotification,
    setError,
    setSuccess,
    setWarning,
    setInfo,
    clearError,
    clearSuccess,
    clearWarning,
    clearInfo
  };

  return (
    <NotificationContext.Provider value={value}>
      {children}
      
      {/* Display notifications */}
      {Object.entries(notifications).map(([type, message]) => 
        message && (
          <Panel key={type} position="top-center" style={{ zIndex: 1000 }}>
            <NotificationPanel 
              type={type} 
              message={message} 
              onDismiss={() => clearNotification(type)} 
            />
          </Panel>
        )
      )}
      
      {/* Include keyframes styles */}
      <NotificationKeyframes />
    </NotificationContext.Provider>
  );
}

// Custom hook to use notifications
export function useNotification() {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotification must be used within a NotificationProvider');
  }
  return context;
}