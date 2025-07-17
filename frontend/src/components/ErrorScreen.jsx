import React from 'react';

function ErrorScreen({ message = "An error occurred." }) {
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
      fontFamily: 'Arial, sans-serif',
      padding: '20px'
    }}>
      <div style={{
        width: '60px',
        height: '60px',
        borderRadius: '50%',
        background: 'rgba(220, 53, 69, 0.1)',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        marginBottom: '20px'
      }}>
        <span style={{
          fontSize: '40px',
          color: '#dc3545'
        }}>!</span>
      </div>
      <h2 style={{ marginBottom: '20px' }}>Error</h2>
      <p style={{ 
        maxWidth: '600px', 
        textAlign: 'center', 
        marginBottom: '30px',
        color: 'rgba(255, 255, 255, 0.7)'
      }}>{message}</p>
    </div>
  );
}

export default ErrorScreen;