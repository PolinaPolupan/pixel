import React, { useState, useEffect } from 'react';
import { executionApi } from '../../services/api.js';
import { IoChevronBack } from 'react-icons/io5';
import './ExecutionPanel.css';

const ExecutionsPanel = ({ onViewFiles }) => {
    const [executions, setExecutions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [filterStatus, setFilterStatus] = useState('ALL');
    const [selectedExecution, setSelectedExecution] = useState(null);
    const [nodeExecutions, setNodeExecutions] = useState([]);
    const [nodeLoading, setNodeLoading] = useState(false);
    const [nodeError, setNodeError] = useState(null);

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

    const fetchNodeExecutions = async (graphExecutionId) => {
        setNodeLoading(true);
        setNodeError(null);
        try {
            const data = await executionApi.getNodeExecutions(graphExecutionId);
            setNodeExecutions(data);
        } catch (err) {
            console.error('Error fetching node executions:', err);
            setNodeError(err.message);
        } finally {
            setNodeLoading(false);
        }
    };

    const handleExecutionClick = (execution) => {
        setSelectedExecution(execution);
        fetchNodeExecutions(execution.id);
    };

    const handleBackClick = () => {
        setSelectedExecution(null);
        setNodeExecutions([]);
        setNodeError(null);
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'COMPLETED':  return '#4caf50';
            case 'RUNNING': return '#2196f3';
            case 'FAILED': return '#f44336';
            case 'PENDING': return '#ff9800';
            default: return '#757575';
        }
    };

    const getStatusIcon = (status) => {
        switch (status) {
            case 'COMPLETED': return '✓';
            case 'RUNNING': return '⟳';
            case 'FAILED': return '✗';
            case 'PENDING': return '○';
            default: return '? ';
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
                minute: '2-digit',
                second: '2-digit'
            });
        } catch (e) {
            return dateString;
        }
    };

    const formatDateTime = (dateString) => {
        if (!dateString) return '-';
        try {
            const date = new Date(dateString);
            return date.toLocaleString('en-US', {
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit',
                fractionalSecondDigits: 3
            });
        } catch (e) {
            return dateString;
        }
    };

    const calculateDuration = (start, end) => {
        if (!start) return '-';
        if (!end) return 'Running... ';
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

    const formatNodeDuration = (start, end) => {
        if (!start || !end) return '-';
        try {
            const duration = new Date(end) - new Date(start);
            const ms = duration % 1000;
            const seconds = Math.floor(duration / 1000);
            return `${seconds}.${ms.toString().padStart(3, '0')}s`;
        } catch (e) {
            return '-';
        }
    };

    const truncateError = (error, maxLength = 100) => {
        if (!error) return null;
        const parts = error.split(':  ');
        const message = parts[parts.length - 1]. trim();
        if (message.length <= maxLength) return message;
        return message.substring(0, maxLength) + '...';
    };

    const handleViewFiles = (graphExecutionId, nodeId) => {
        if (onViewFiles) {
            onViewFiles(graphExecutionId, nodeId);
        }
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

    // Render node executions view
    if (selectedExecution) {
        return (
            <div className="executions-panel">
                <div className="executions-header">
                    <button
                        onClick={handleBackClick}
                        className="back-btn"
                        title="Back to executions"
                    >
                        <IoChevronBack size={20} />
                    </button>
                    <div className="executions-header-info">
                        <h3>Node Executions</h3>
                        <span className="executions-subtitle">
                            #{selectedExecution.id} - {selectedExecution.graphId}
                        </span>
                    </div>
                </div>

                {nodeError && (
                    <div className="executions-error">
                        ⚠ {nodeError}
                    </div>
                )}

                <div className="executions-list">
                    {nodeLoading ?  (
                        <div className="executions-empty">
                            Loading node executions...
                        </div>
                    ) : nodeExecutions.length === 0 ? (
                        <div className="executions-empty">
                            No node executions found
                        </div>
                    ) : (
                        nodeExecutions.map((node) => (
                            <div key={node.id} className="execution-item node-execution-item">
                                <div className="execution-header-row">
                                    <div className="execution-header-left">
                                        <span
                                            className="execution-status"
                                            style={{ color: getStatusColor(node.status) }}
                                        >
                                            <span className="status-icon">{getStatusIcon(node.status)}</span>
                                            {node.status}
                                        </span>
                                        {node.nodeType && (
                                            <span className="execution-node-type-badge">
                                                {node.nodeType}
                                            </span>
                                        )}
                                    </div>
                                    <span className="execution-id">Node #{node.id}</span>
                                </div>

                                <div className="execution-info">
                                    <div className="execution-row">
                                        <span className="execution-label">Type:</span>
                                        <span className="execution-value execution-node-type">
                                            {node.nodeType || '-'}
                                        </span>
                                    </div>

                                    <button
                                        className="view-files-btn"
                                        onClick={() => handleViewFiles(selectedExecution.id, node.nodeId)}
                                    >
                                        📁 View Files
                                    </button>

                                    <div className="execution-row">
                                        <span className="execution-label">Started:</span>
                                        <span className="execution-value execution-time">
                                            {formatDateTime(node.startedAt)}
                                        </span>
                                    </div>

                                    <div className="execution-row">
                                        <span className="execution-label">Finished:</span>
                                        <span className="execution-value execution-time">
                                            {formatDateTime(node.finishedAt)}
                                        </span>
                                    </div>

                                    <div className="execution-row">
                                        <span className="execution-label">Duration:</span>
                                        <span className="execution-value">
                                            {formatNodeDuration(node.startedAt, node.finishedAt)}
                                        </span>
                                    </div>

                                    {node.errorMessage && (
                                        <div className="execution-error-message" title={node.errorMessage}>
                                            <strong>Error:</strong> {truncateError(node.errorMessage)}
                                        </div>
                                    )}

                                    {node.inputs && Object.keys(node.inputs).length > 0 && (
                                        <details className="execution-details-section">
                                            <summary>Inputs ({Object.keys(node.inputs).length})</summary>
                                            <pre className="execution-json">
                                                {JSON. stringify(node.inputs, null, 2)}
                                            </pre>
                                        </details>
                                    )}

                                    {node.outputs && Object.keys(node.outputs).length > 0 && (
                                        <details className="execution-details-section">
                                            <summary>Outputs ({Object.keys(node.outputs).length})</summary>
                                            <pre className="execution-json">
                                                {JSON.stringify(node.outputs, null, 2)}
                                            </pre>
                                        </details>
                                    )}
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </div>
        );
    }

    // Render graph executions list
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
                {['ALL', 'COMPLETED', 'FAILED', 'RUNNING'].map(status => (
                    <button
                        key={status}
                        className={`filter-btn ${filterStatus === status ? 'active' :  ''}`}
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
                        {loading ? 'Loading.. .' : 'No executions found'}
                    </div>
                ) : (
                    filteredExecutions.map((execution) => (
                        <div
                            key={execution.id}
                            className="execution-item execution-item-clickable"
                            onClick={() => handleExecutionClick(execution)}
                        >
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
                                        {calculateDuration(execution.startTime, execution.endTime)}
                                    </span>
                                </div>

                                {execution.errorMessage && (
                                    <div className="execution-error-message" title={execution.errorMessage}>
                                        <strong>Error:</strong> {truncateError(execution. errorMessage)}
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