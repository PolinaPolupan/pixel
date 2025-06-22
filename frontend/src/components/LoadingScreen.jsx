import React from 'react';

function LoadingScreen({ message = "Loading..." }) {
  return (
    <div style={{
      width: '100vw',
      height: '100vh',
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'center',
      alignItems: 'center',
      background: '#1a1a1a',
      color: 'white',
      fontFamily: 'Arial, sans-serif'
    }}>
      <div style={{
        width: '40px',
        height: '40px',
        border: '3px solid rgba(255, 255, 255, 0.3)',
        borderRadius: '50%',
        borderTopColor: 'white',
        animation: 'spin 1s linear infinite',
        marginBottom: '20px'
      }} />
      <style>{`
        @keyframes spin {
          to { transform: rotate(360deg); }
        }
      `}</style>
      <p>{message}</p>
    </div>
  );
}

export default LoadingScreen;