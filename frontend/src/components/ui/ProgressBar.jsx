import React from 'react';
import { useProgress } from '../../services/contexts/ProgressContext.jsx';
import './ProgressBar.css';

export default function ProgressBar() {
    const { progressState } = useProgress();

    return (
        <div className="progress-bar-container">
            {progressState.visible ?  (
                <div className={`progress-bar-wrapper ${progressState.fadeOut ? 'fade-out' : ''}`}>
                    <div
                        className="progress-bar-fill"
                        style={{ width: `${progressState.percent}%` }}
                    />
                    <div className="progress-bar-text">
                        {progressState.current} / {progressState.total} nodes processed ({progressState.percent}%)
                    </div>
                </div>
            ) : (
                <div className="progress-bar-ready">
                    Ready for processing
                </div>
            )}
        </div>
    );
}