import React from 'react';

const playButtonStyle = {
  width: '40px',
  height: '40px',
  borderRadius: '8px',
  border: '1px solid rgba(255, 255, 255, 0.2)',
  color: 'rgb(194, 255, 212)',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  fontSize: '18px',
  padding: 0,
  lineHeight: 1,
  boxSizing: 'border-box',
  boxShadow: '0 2px 4px rgba(0, 0, 0, 0.3)',
  transition: 'background 0.2s, border-color 0.2s, transform 0.1s'
};

export const PlayButton = ({ onClick, isProcessing }) => {
  return (
    <button
      onClick={onClick}
      disabled={isProcessing}
      style={{
        ...playButtonStyle,
        background: isProcessing ? 'rgb(100, 100, 100)' : 'rgb(0, 110, 0)',
        cursor: isProcessing ? 'wait' : 'pointer',
      }}
      onMouseOver={(e) => {
        if (!isProcessing) {
          e.target.style.background = 'rgb(0, 200, 0)';
          e.target.style.borderColor = 'rgba(255, 255, 255, 0.4)';
          e.target.style.transform = 'scale(1.05)';
        }
      }}
      onMouseOut={(e) => {
        if (!isProcessing) {
          e.target.style.background = 'rgb(0, 110, 0)';
          e.target.style.borderColor = 'rgba(255, 255, 255, 0.2)';
          e.target.style.transform = 'scale(1)';
        }
      }}
    >
      {isProcessing ? '⏳' : '▶'}
    </button>
  );
};