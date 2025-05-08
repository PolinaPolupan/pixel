import React, { useCallback, useState, useEffect } from 'react';
import { useReactFlow, useStoreApi } from '@xyflow/react';
import { useNodesApi } from '../utils/useNodesApi';

const NodeTypesPanel = () => {
    const { getNodes, addNodes, screenToFlowPosition } = useReactFlow();
    const store = useStoreApi();
    const {
        nodesByCategory,
        sortedCategories,
        filterNodes,
        getDefaultData,
        isLoading,
        error
    } = useNodesApi();

    const [searchTerm, setSearchTerm] = useState('');
    const [activeCategory, setActiveCategory] = useState('All');

    // Track expanded categories
    const [expandedCategories, setExpandedCategories] = useState({});

    // Initialize expanded state for categories
    useEffect(() => {
        if (sortedCategories.length > 0) {
            const initialExpanded = {};
            // Default all categories to collapsed
            sortedCategories.forEach(category => {
                initialExpanded[category] = false;
            });
            setExpandedCategories(initialExpanded);
        }
    }, [sortedCategories]);

    // Toggle category expansion
    const toggleCategory = (category) => {
        setExpandedCategories(prev => ({
            ...prev,
            [category]: !prev[category]
        }));
    };

    // Get filtered nodes based on search and active category
    const visibleNodesByCategory = filterNodes(searchTerm, activeCategory === 'All' ? 'All' : activeCategory);
    const visibleCategories = Object.keys(visibleNodesByCategory).sort();
    const hasNodes = visibleCategories.length > 0;

    const getViewportCenterNode = useCallback((type) => {
        const { domNode } = store.getState();
        const boundingRect = domNode?.getBoundingClientRect();

        if (!boundingRect) {
            // Fallback position if domNode is unavailable
            return { position: { x: 100, y: 100 }, width: 150, height: 60 };
        }

        // Calculate viewport center in screen coordinates
        const center = screenToFlowPosition({
            x: boundingRect.x + boundingRect.width / 2,
            y: boundingRect.y + boundingRect.height / 2,
        });

        // Estimate node dimensions (based on default sizes)
        const nodeDimensions = { width: 150, height: 60 };

        // Adjust position to center node
        const position = {
            x: center.x - nodeDimensions.width / 2,
            y: center.y - nodeDimensions.height / 2,
        };

        return { position, ...nodeDimensions };
    }, [screenToFlowPosition, store]);

    const createNode = (type) => {
        const nodeIds = getNodes().map(node => parseInt(node.id));
        const newId = (Math.max(...nodeIds, 0) + 1).toString();
        const { position } = getViewportCenterNode(type);

        const newNode = {
            id: newId,
            type,
            position,
            data: getDefaultData(type)
        };

        addNodes(newNode);
    };

    const renderIcon = (icon, type) => {
        if (!icon) return null;
        const IconComponent = icon;
        return (
            <IconComponent
                style={{ fontSize: '16px', color: 'var(--on-surface)', transition: 'transform 0.2s' }}
                className="visual-hover"
                type={type}
            />
        );
    };

    // Loading state
    if (isLoading) {
        return (
            <div style={{
                width: '100%',
                height: '100%',
                padding: '16px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#ffffff',
            }}>
                <div>Loading node types...</div>
            </div>
        );
    }

    // Error state
    if (error) {
        return (
            <div style={{
                width: '100%',
                height: '100%',
                padding: '16px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#ff6b6b',
            }}>
                <div>Error loading node types: {error}</div>
            </div>
        );
    }

    return (
        <div style={{
            width: '100%',
            height: '100%',
            padding: '16px',
            fontFamily: 'Arial, sans-serif',
            color: '#ffffff',
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden',
        }}>
            {/* Search box */}
            <div style={{ marginBottom: '12px' }}>
                <input
                    type="text"
                    placeholder="Search nodes..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    style={{
                        width: '100%',
                        padding: '8px',
                        backgroundColor: 'rgba(255, 255, 255, 0.1)',
                        border: '1px solid rgba(255, 255, 255, 0.2)',
                        borderRadius: '4px',
                        color: 'white',
                        fontSize: '13px',
                        outline: 'none'
                    }}
                />
            </div>

            {/* Node listing with foldable categories */}
            <div style={{
                flex: 1,
                overflowY: 'auto',
                marginBottom: '12px',
                marginRight: '12px'
            }}>
                {hasNodes ? (
                    visibleCategories.map(category => (
                        <div key={category} style={{ marginBottom: '8px' }}>
                            <div
                                onClick={() => toggleCategory(category)}
                                style={{
                                    fontSize: '14px',
                                    fontWeight: 'bold',
                                    color: 'rgba(255, 255, 255, 0.8)',
                                    padding: '8px 10px',
                                    borderRadius: '4px',
                                    background: 'rgba(255, 255, 255, 0.05)',
                                    cursor: 'pointer',
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                    marginBottom: '4px',
                                    userSelect: 'none'
                                }}
                            >
                                <span>{category}</span>
                                <span style={{ fontSize: '12px', fontWeight: 'normal' }}>
                  {expandedCategories[category] ? '▼' : '▶'}
                </span>
                            </div>

                            {/* Show nodes only if category is expanded */}
                            {expandedCategories[category] && (
                                <div style={{
                                    display: 'flex',
                                    flexDirection: 'column',
                                    gap: '8px',
                                    padding: '0 4px'
                                }}>
                                    {visibleNodesByCategory[category].map(({ type, details }) => (
                                        <div
                                            key={type}
                                            onClick={() => createNode(type)}
                                            style={{
                                                background: `linear-gradient(45deg, ${details.color}11, ${details.color}5)`,
                                                border: `1px solid ${details.color}22`,
                                                borderRadius: '6px',
                                                padding: '8px 10px',
                                                cursor: 'pointer',
                                                transition: 'all 0.2s ease',
                                                color: '#ffffff',
                                                fontSize: '13px',
                                                display: 'flex',
                                                alignItems: 'center',
                                                gap: '8px',
                                                marginRight: '12px'
                                            }}
                                            onMouseOver={(e) => {
                                                e.currentTarget.style.border = `1px solid ${details.color}44`;
                                                e.currentTarget.style.transform = 'translateY(-1px)';
                                                e.currentTarget.style.boxShadow = '0 4px 8px rgba(0,0,0,0.15)';
                                            }}
                                            onMouseOut={(e) => {
                                                e.currentTarget.style.border = `1px solid ${details.color}22`;
                                                e.currentTarget.style.transform = 'translateY(0)';
                                                e.currentTarget.style.boxShadow = 'none';
                                            }}
                                        >
                                            {details.icon && renderIcon(details.icon, type)}
                                            <div>
                                                <div style={{ fontWeight: 'bold', marginBottom: '2px' }}>{type}</div>
                                                <div style={{ fontSize: '11px', opacity: '0.7' }}>{details.description}</div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    ))
                ) : (
                    <div style={{
                        color: 'rgba(255, 255, 255, 0.5)',
                        textAlign: 'center',
                        padding: '20px 0'
                    }}>
                        No nodes match your search
                    </div>
                )}
            </div>

            <div style={{
                fontSize: '11px',
                color: 'rgba(255, 255, 255, 0.5)',
                textAlign: 'center',
                paddingTop: '8px',
                borderTop: '1px solid rgba(255, 255, 255, 0.1)'
            }}>
                Tip: Right-click anywhere to add a node at cursor position
            </div>
        </div>
    );
};

export default NodeTypesPanel;