import React from 'react';
import './PlayButton.css';

export const PlayButton = ({ onClick, isProcessing, disabled }) => {
    return (
        <button
            onClick={onClick}
            disabled={isProcessing || disabled}
            className="play-button"
        >
            {isProcessing ?  '⏳' : '▶'}
        </button>
    );
};