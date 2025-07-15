import React from 'react';
import { useScene } from './contexts/SceneContext.jsx';

function ErrorScreen({ message = "An error occurred." }) {
  const { createNewScene } = useScene();
  
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
      <button 
        onClick={createNewScene}
        style={{
          padding: '10px 16px',
          background: '#3c4fe0',
          color: 'white',
          border: 'none',
          borderRadius: '4px',
          cursor: 'pointer',
          fontWeight: 'bold'
        }}
      >
        Try Again
      </button>
    </div>
  );
}

export default ErrorScreen;