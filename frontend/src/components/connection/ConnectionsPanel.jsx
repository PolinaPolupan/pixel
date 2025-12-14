import React, { useState, useEffect } from 'react';
import { IoAdd, IoTrash, IoRefreshCircle, IoClose } from 'react-icons/io5';
import './ConnectionsPanel.css';

const ConnectionsPanel = () => {
    const [connections, setConnections] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [showForm, setShowForm] = useState(false);
    const [formData, setFormData] = useState({
        connId: '',
        connType: 'postgres',
        host: '',
        schema: '',
        login: '',
        password: '',
        port: 5432,
        extra: ''
    });

    useEffect(() => {
        fetchConnections();
    }, []);

    const fetchConnections = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch('http://localhost:8080/v1/connections', {
                credentials: 'include'
            });
            if (!response.ok) throw new Error('Failed to fetch connections');
            const data = await response. json();
            setConnections(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleCreate = async (e) => {
        e.preventDefault();
        setError(null);
        try {
            const response = await fetch('http://localhost:8080/v1/connections', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify(formData)
            });
            if (!response.ok) throw new Error('Failed to create connection');
            await fetchConnections();
            setShowForm(false);
            resetForm();
        } catch (err) {
            setError(err.message);
        }
    };

    const handleDelete = async (connId) => {
        if (! confirm(`Delete connection "${connId}"?`)) return;
        setError(null);
        try {
            const response = await fetch(`http://localhost:8080/v1/connections/${connId}`, {
                method: 'DELETE',
                credentials: 'include'
            });
            if (!response.ok) throw new Error('Failed to delete connection');
            await fetchConnections();
        } catch (err) {
            setError(err. message);
        }
    };

    const resetForm = () => {
        setFormData({
            connId: '',
            connType: 'postgres',
            host: '',
            schema: '',
            login: '',
            password: '',
            port: 5432,
            extra: ''
        });
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]:  name === 'port' ? parseInt(value) || 0 : value
        }));
    };

    return (
        <div className="connections-panel">
            <div className="connections-header">
                <h3>Connections</h3>
                <div className="connections-header-actions">
                    <button onClick={fetchConnections} className="icon-btn" title="Refresh">
                        <IoRefreshCircle size={18} />
                    </button>
                    <button onClick={() => setShowForm(true)} className="icon-btn primary" title="Add Connection">
                        <IoAdd size={18} />
                    </button>
                </div>
            </div>

            {error && (
                <div className="connections-error">
                    ⚠ {error}
                </div>
            )}

            {showForm && (
                <div className="connection-form-overlay" onClick={() => setShowForm(false)}>
                    <div className="connection-form-modal" onClick={(e) => e.stopPropagation()}>
                        <div className="connection-form-header">
                            <h4>New Connection</h4>
                            <button onClick={() => setShowForm(false)} className="close-btn">
                                <IoClose size={20} />
                            </button>
                        </div>
                        <form onSubmit={handleCreate} className="connection-form">
                            <div className="form-group">
                                <label>Connection ID *</label>
                                <input
                                    type="text"
                                    name="connId"
                                    value={formData.connId}
                                    onChange={handleChange}
                                    required
                                    placeholder="my_database"
                                />
                            </div>

                            <div className="form-group">
                                <label>Type *</label>
                                <input
                                    type="text"
                                    name="connType"
                                    value={formData.connType}
                                    onChange={handleChange}
                                    required
                                    placeholder="postgres"
                                />
                            </div>
                            <div className="form-row">
                                <div className="form-group">
                                    <label>Host *</label>
                                    <input
                                        type="text"
                                        name="host"
                                        value={formData.host}
                                        onChange={handleChange}
                                        required
                                        placeholder="localhost"
                                    />
                                </div>

                                <div className="form-group">
                                    <label>Port *</label>
                                    <input
                                        type="number"
                                        name="port"
                                        value={formData.port}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                            </div>

                            <div className="form-group">
                                <label>Schema</label>
                                <input
                                    type="text"
                                    name="schema"
                                    value={formData. schema}
                                    onChange={handleChange}
                                    placeholder="public"
                                />
                            </div>

                            <div className="form-group">
                                <label>Login</label>
                                <input
                                    type="text"
                                    name="login"
                                    value={formData. login}
                                    onChange={handleChange}
                                    placeholder="username"
                                />
                            </div>

                            <div className="form-group">
                                <label>Password</label>
                                <input
                                    type="password"
                                    name="password"
                                    value={formData.password}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="form-group">
                                <label>Extra (JSON)</label>
                                <textarea
                                    name="extra"
                                    value={formData.extra}
                                    onChange={handleChange}
                                    placeholder='{"ssl": true}'
                                    rows={3}
                                />
                            </div>

                            <div className="form-actions">
                                <button type="button" onClick={() => setShowForm(false)} className="btn-secondary">
                                    Cancel
                                </button>
                                <button type="submit" className="btn-primary">
                                    Create
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            <div className="connections-list">
                {loading ?  (
                    <div className="connections-empty">Loading... </div>
                ) : connections.length === 0 ? (
                    <div className="connections-empty">
                        No connections yet. Click + to add one.
                    </div>
                ) : (
                    connections.map(conn => (
                        <div key={conn.connId} className="connection-item">
                            <div className="connection-header">
                                <div className="connection-info">
                                    <div className="connection-id">{conn.connId}</div>
                                    <div className="connection-type">{conn. connType}</div>
                                </div>
                                <button
                                    onClick={() => handleDelete(conn.connId)}
                                    className="delete-btn"
                                    title="Delete"
                                >
                                    <IoTrash size={16} />
                                </button>
                            </div>
                            <div className="connection-details">
                                <div className="connection-detail">
                                    <span className="label">Host:</span>
                                    <span className="value">{conn.host}</span>
                                </div>
                                {conn.login && (
                                    <div className="connection-detail">
                                        <span className="label">Login:</span>
                                        <span className="value">{conn.login}</span>
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

export default ConnectionsPanel;