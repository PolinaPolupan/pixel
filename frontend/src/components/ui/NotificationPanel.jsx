import React from 'react';

const baseNotificationStyle = {
  background: 'rgba(40, 40, 40, 0.9)',
  borderRadius: '8px',
  padding: '12px 16px',
  fontFamily: 'Arial, sans-serif',
  fontSize: '14px',
  minWidth: '300px',
  boxShadow: '0 4px 12px rgba(0, 0, 0, 0.4)',
  position: 'absolute',
  top: '20px',
  left: '50%',
  transform: 'translateX(-50%)',
  transformOrigin: 'center center',
  animation: 'fadeInOut 5s ease-in-out forwards',
};

export const NotificationPanel = ({ type, message, onDismiss }) => {
  const colors = type === 'error' 
    ? { border: 'rgba(255, 85, 85, 0.6)', text: '#ff6666', icon: '#ff9999', hover: '#ffcccc' }
    : { border: 'rgba(85, 255, 85, 0.6)', text: '#66ff66', icon: '#99ff99', hover: '#ccffcc' };
  
  const icon = type === 'error' ? '⚠️' : '✅';
  
  return (
    <div
      style={{
        ...baseNotificationStyle,
        border: `1px solid ${colors.border}`,
        color: colors.text,
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '12px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ fontSize: '18px', color: colors.icon }}>{icon}</span>
          <span>{message}</span>
        </div>
        <button
          onClick={onDismiss}
          style={{
            background: 'none',
            border: 'none',
            color: colors.icon,
            fontSize: '16px',
            cursor: 'pointer',
            padding: '0',
            transition: 'color 0.2s',
          }}
          onMouseOver={(e) => (e.target.style.color = colors.hover)}
          onMouseOut={(e) => (e.target.style.color = colors.icon)}
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