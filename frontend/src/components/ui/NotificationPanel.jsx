import React from 'react';
import './NotificationPanel.css';

export const NotificationPanel = ({ type, message, onDismiss }) => {
    const isError = type === 'error';
    const icon = isError ? '⚠️' : '✅';

    return (
        <div className={`notification-panel ${isError ? 'notification-panel-error' : 'notification-panel-success'}`}>
            <div className="notification-panel-content">
                <div className="notification-panel-message">
                    <span className={`notification-panel-icon ${isError ? 'notification-panel-icon-error' : 'notification-panel-icon-success'}`}>
                        {icon}
                    </span>
                    <span>{message}</span>
                </div>
                <button
                    onClick={onDismiss}
                    className={`notification-panel-close ${isError ? 'notification-panel-close-error' : 'notification-panel-close-success'}`}
                >
                    ×
                </button>
            </div>
        </div>
    );
};

export const NotificationKeyframes = () => (
  <style>
    {`
      @keyframes fadeInOut {
        0% { opacity: 0; transform: translateY(-20px) translateX(-50%); }
        10% { opacity: 1; transform: translateY(0) translateX(-50%); }
        90% { opacity: 1; transform: translateY(0) translateX(-50%); }
        100% { opacity: 0; transform: translateY(-20px) translateX(-50%); }
      }
    `}
  </style>
);