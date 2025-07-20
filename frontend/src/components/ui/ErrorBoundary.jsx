import React from 'react';
import ErrorScreen from './ErrorScreen.jsx';

class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false, error: null };
    }

    static getDerivedStateFromError(error) {
        return { hasError: true, error };
    }

    componentDidCatch(error, errorInfo) {
        console.error('Application error:', error, errorInfo);
    }

    render() {
        if (this.state.hasError) {
            return <ErrorScreen message={this.state.error?.message || 'An unexpected error occurred'} />;
        }

        return this.props.children;
    }
}

export default ErrorBoundary;