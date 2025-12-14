import React, { useState, useEffect } from 'react';
import { IoClose } from 'react-icons/io5';
import { executionApi } from '../../services/api.js';
import './ExecutionPanel.css';

const NodeExecutionModal = ({ graphExecution, onClose }) => {
    const [nodeExecutions, setNodeExecutions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (graphExecution) {
            fetchNodeExecutions();
        }
    }, [graphExecution]);

    const fetchNodeExecutions = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await executionApi.getNodeExecutions(graphExecution.id);
            setNodeExecutions(data);
        } catch (err) {
            console.error('Error fetching node executions:', err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
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
            case 'FAILED':  return '✗';
            case 'PENDING': return '○';
            default: return '? ';
        }
    };

    const formatDate = (dateString) => {
        if (!dateString) return '-';
        try {
            const date = new Date(dateString);
            return date.toLocaleString('en-US', {
                hour: '2-digit',
                minute:  '2-digit',
                second: '2-digit',
                fractionalSecondDigits: 3
            });
        } catch (e) {
            return dateString;
        }
    };

    const formatDuration = (start, end) => {
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

    if (! graphExecution) return null;

    return (
        <div className="node-execution-modal-overlay" onClick={onClose}>
            <div className="node-execution-modal" onClick={(e) => e.stopPropagation()}>
                <div className="node-execution-modal-header">
                    <div>
                        <h3>Node Executions</h3>
                        <span className="node-execution-modal-subtitle">
                            Graph Execution #{graphExecution.id} - {graphExecution.graphId}
                        </span>
                    </div>
                    <button onClick={onClose} className="node-execution-modal-close">
                        <IoClose size={24} />
                    </button>
                </div>

                <div className="node-execution-modal-content">
                    {loading && (
                        <div className="node-execution-modal-loading">
                            Loading node executions...
                        </div>
                    )}

                    {error && (
                        <div className="node-execution-modal-error">
                            ⚠ Error:  {error}
                        </div>
                    )}

                    {!loading && !error && nodeExecutions.length === 0 && (
                        <div className="node-execution-modal-empty">
                            No node executions found
                        </div>
                    )}

                    {!loading && !error && nodeExecutions.length > 0 && (
                        <div className="node-execution-list">
                            {nodeExecutions.map((node) => (
                                <div key={node.id} className="node-execution-card">
                                    <div className="node-execution-card-header">
                                        <span
                                            className="node-execution-status"
                                            style={{ color: getStatusColor(node.status) }}
                                        >
                                            <span className="status-icon">{getStatusIcon(node.status)}</span>
                                            {node.status}
                                        </span>
                                        <span className="node-execution-id">Node #{node.id}</span>
                                    </div>

                                    <div className="node-execution-details">
                                        <div className="node-execution-row">
                                            <span className="node-execution-label">Started:</span>
                                            <span className="node-execution-value">{formatDate(node.startedAt)}</span>
                                        </div>
                                        <div className="node-execution-row">
                                            <span className="node-execution-label">Finished:</span>
                                            <span className="node-execution-value">{formatDate(node.finishedAt)}</span>
                                        </div>
                                        <div className="node-execution-row">
                                            <span className="node-execution-label">Duration:</span>
                                            <span className="node-execution-value">
                                                {formatDuration(node.startedAt, node.finishedAt)}
                                            </span>
                                        </div>

                                        {node.errorMessage && (
                                            <div className="node-execution-error">
                                                <strong>Error:</strong> {node.errorMessage}
                                            </div>
                                        )}

                                        {node.inputs && Object.keys(node.inputs).length > 0 && (
                                            <details className="node-execution-details-section">
                                                <summary>Inputs</summary>
                                                <pre className="node-execution-json">
                                                    {JSON. stringify(node.inputs, null, 2)}
                                                </pre>
                                            </details>
                                        )}

                                        {node.outputs && Object.keys(node.outputs).length > 0 && (
                                            <details className="node-execution-details-section">
                                                <summary>Outputs</summary>
                                                <pre className="node-execution-json">
                                                    {JSON.stringify(node.outputs, null, 2)}
                                                </pre>
                                            </details>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default NodeExecutionModal;