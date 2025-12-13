import React from 'react';
import './ErrorScreen.css';

function ErrorScreen({ message = "An error occurred." }) {
    return (
        <div className="error-screen">
            <div className="error-screen-icon">
                <span className="error-screen-icon-symbol">! </span>
            </div>
            <h2 className="error-screen-title">Error</h2>
            <p className="error-screen-message">{message}</p>
        </div>
    );
}

export default ErrorScreen;