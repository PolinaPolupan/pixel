import React from 'react';
import { useProgress } from './contexts/ProgressContext.jsx';

export default function ProgressBar() {
    const { progressState } = useProgress();

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