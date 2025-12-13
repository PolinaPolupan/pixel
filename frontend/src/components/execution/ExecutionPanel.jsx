import React, { useState, useEffect } from 'react';
import { executionApi } from '../../services/api.js';
import './ExecutionPanel.css';

const ExecutionsPanel = () => {
    const [executions, setExecutions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [filterStatus, setFilterStatus] = useState('ALL');

    useEffect(() => {
        fetchExecutions();
        const interval = setInterval(fetchExecutions, 5000);
        return () => clearInterval(interval);
    }, []);

    const fetchExecutions = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await executionApi.getAll();
            setExecutions(data);
        } catch (err) {
            console.error('Error fetching executions:', err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'COMPLETED':  return '#4caf50';
            case 'RUNNING':  return '#2196f3';
            case 'FAILED': return '#f44336';
            case 'PENDING': return '#ff9800';
            default: return '#757575';
        }
    };

    const getStatusIcon = (status) => {
        switch (status) {
            case 'COMPLETED':  return '✓';
            case 'RUNNING': return '⟳';
            case 'FAILED': return '✗';
            case 'PENDING':  return '○';
            default:  return '? ';
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return '-';
        try {
            const date = new Date(dateString);
            return date.toLocaleString('en-US', {
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute:  '2-digit',
                second: '2-digit'
            });
        } catch (e) {
            return dateString;
        }
    };

    const calculateDuration = (start, end) => {
        if (!start) return '-';
        if (! end) return 'Running...';
        try {
            const duration = new Date(end) - new Date(start);
            const seconds = Math.floor(duration / 1000);
            if (seconds < 60) return `${seconds}s`;
            const minutes = Math.floor(seconds / 60);
            const remainingSeconds = seconds % 60;
            return `${minutes}m ${remainingSeconds}s`;
        } catch (e) {
            return '-';
        }
    };

    const truncateError = (error, maxLength = 100) => {
        if (!error) return null;
        const parts = error.split(': ');
        const message = parts[parts.length - 1]. trim();
        if (message. length <= maxLength) return message;
        return message.substring(0, maxLength) + '...';
    };

    const filteredExecutions = filterStatus === 'ALL'
        ? executions
        :  executions.filter(e => e.status === filterStatus);

    const statusCounts = {
        ALL: executions.length,
        COMPLETED: executions.filter(e => e.status === 'COMPLETED').length,
        FAILED: executions.filter(e => e.status === 'FAILED').length,
        RUNNING:  executions.filter(e => e.status === 'RUNNING').length,
    };

    return (
        <div className="executions-panel">
            <div className="executions-header">
                <h3>Executions</h3>
                <button
                    onClick={fetchExecutions}
                    className="refresh-btn"
                    disabled={loading}
                    title="Refresh executions"
                >
                    {loading ? '⟳' : '↻'}
                </button>
            </div>

            <div className="executions-filters">
                {['ALL', 'COMPLETED', 'FAILED', 'RUNNING']. map(status => (
                    <button
                        key={status}
                        className={`filter-btn ${filterStatus === status ?  'active' : ''}`}
                        onClick={() => setFilterStatus(status)}
                    >
                        {status} ({statusCounts[status] || 0})
                    </button>
                ))}
            </div>

            {error && (
                <div className="executions-error">
                    ⚠ {error}
                </div>
            )}

            <div className="executions-list">
                {filteredExecutions.length === 0 ? (
                    <div className="executions-empty">
                        {loading ?  'Loading...' : 'No executions found'}
                    </div>
                ) : (
                    filteredExecutions. map((execution) => (
                        <div key={execution.id} className="execution-item">
                            <div className="execution-header-row">
                                <span
                                    className="execution-status"
                                    style={{ color: getStatusColor(execution.status) }}
                                >
                                    <span className="status-icon">{getStatusIcon(execution.status)}</span>
                                    {execution.status}
                                </span>
                                <span className="execution-id">#{execution.id}</span>
                            </div>

                            <div className="execution-info">
                                <div className="execution-row">
                                    <span className="execution-label">Graph:</span>
                                    <span className="execution-value execution-graph-id" title={execution.graphId}>
                                        {execution.graphId}
                                    </span>
                                </div>

                                <div className="execution-row">
                                    <span className="execution-label">Progress:</span>
                                    <span className="execution-value">
                                        {execution.processedNodes || 0} / {execution.totalNodes || 0}
                                    </span>
                                </div>

                                {execution.totalNodes > 0 && (
                                    <div className="execution-progress-bar">
                                        <div
                                            className="execution-progress-fill"
                                            style={{
                                                width: `${((execution.processedNodes || 0) / execution.totalNodes) * 100}%`,
                                                backgroundColor: getStatusColor(execution.status)
                                            }}
                                        />
                                    </div>
                                )}

                                <div className="execution-row">
                                    <span className="execution-label">Started:</span>
                                    <span className="execution-value execution-time">
                                        {formatDate(execution.startTime)}
                                    </span>
                                </div>

                                <div className="execution-row">
                                    <span className="execution-label">Duration:</span>
                                    <span className="execution-value">
                                        {calculateDuration(execution.startTime, execution. endTime)}
                                    </span>
                                </div>

                                {execution.errorMessage && (
                                    <div className="execution-error-message" title={execution.errorMessage}>
                                        <strong>Error: </strong> {truncateError(execution.errorMessage)}
                                    </div>
                                )}
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
};

export default ExecutionsPanel;