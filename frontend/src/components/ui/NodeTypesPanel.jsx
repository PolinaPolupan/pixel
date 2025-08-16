import React, { useCallback, useState, useEffect } from 'react';
import { useReactFlow, useStoreApi } from '@xyflow/react';
import { useNodesApi } from '../../hooks/useNodesApi.js';
import '../../styles/App.css';

const NodeTypesPanel = () => {
    const { getNodes, addNodes, screenToFlowPosition } = useReactFlow();
    const store = useStoreApi();
    const {
        sortedCategories,
        filterNodes,
        getDefaultData,
        isLoading,
        error
    } = useNodesApi();

    const [searchTerm, setSearchTerm] = useState('');
    const [activeCategory] = useState('All');
    const [hoveredCategory, setHoveredCategory] = useState(null);
    const [hoveredNode, setHoveredNode] = useState(null);

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

    const getViewportCenterNode = useCallback(() => {
        const { domNode } = store.getState();
        const boundingRect = domNode?.getBoundingClientRect();

        if (!boundingRect) {
            return { position: { x: 100, y: 100 }, width: 150, height: 60 };
        }

        const center = screenToFlowPosition({
            x: boundingRect.x + boundingRect.width / 2,
            y: boundingRect.y + boundingRect.height / 2,
        });

        const nodeDimensions = { width: 150, height: 60 };

        const position = {
            x: center.x - nodeDimensions.width / 2,
            y: center.y - nodeDimensions.height / 2,
        };

        return { position, ...nodeDimensions };
    }, [screenToFlowPosition, store]);

    const { nodesConfig } = useNodesApi();

    const createNode = (type) => {
        const nodeIds = getNodes().map(node => parseInt(node.id));
        const newId = (Math.max(...nodeIds, 0) + 1).toString();
        const { position } = getViewportCenterNode(type);

        addNodes({
            id: newId,
            type,
            position,
            data: {
                ...getDefaultData(type),
                config: nodesConfig[type]
            }
        });
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

    // Generate gradient styles based on hover state
    const getCategoryGradient = (category, color) => {
        // Normal gradient (when not hovered)
        const normalGradient = `linear-gradient(45deg, ${color}11, rgba(255, 255, 255, 0))`;

        // Brighter gradient (when hovered)
        const brightGradient = `linear-gradient(90deg, ${color}13, rgba(255, 255, 255, 0))`;

        return category === hoveredCategory ? brightGradient : normalGradient;
    };

    // Loading state
    if (isLoading) {
        return (
            <div className="node-types-panel">
                <div>Loading node types...</div>
            </div>
        );
    }

    // Error state
    if (error) {
        return (
            <div className="node-types-panel">
                <div style={{ color: '#ff6b6b' }}>Error loading node types: {error}</div>
            </div>
        );
    }

    return (
        <div className="node-types-panel">
            {/* Search box */}
            <div style={{ marginBottom: '12px', width: '100%', display: 'flex' }}>
                <input
                    type="text"
                    className="text-field-alternative"
                    placeholder="Search nodes..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />
            </div>

            {/* Node listing with foldable categories */}
            <div style={{ flex: 1,  marginBottom: '12px' }}>
                {hasNodes ? (
                    visibleCategories.map(category => {
                        // Get the first node's color for the category gradient
                        const firstNodeColor = visibleNodesByCategory[category][0]?.details?.color || '#ffffff';

                        return (
                            <div key={category} style={{ marginBottom: '8px' }}>
                                <div
                                    onClick={() => toggleCategory(category)}
                                    className="node-category"
                                    onMouseEnter={() => setHoveredCategory(category)}
                                    onMouseLeave={() => setHoveredCategory(null)}
                                    style={{
                                        background: getCategoryGradient(category, firstNodeColor)
                                    }}
                                >
                                    <span style={{ color: "rgba(255,255,255,0.8)" }}>{category}</span>
                                    <span
                                        style={{ color: "rgba(255,255,255,0.5)" }}
                                        className={`arrow-icon ${expandedCategories[category] ? 'expanded' : ''}`}>
                                        {expandedCategories[category] ? '▼' : '▶'}
                                    </span>
                                </div>

                                {/* Node items container with animation */}
                                <div className={`node-items-container ${expandedCategories[category] ? 'expanded' : ''}`}>
                                    {visibleNodesByCategory[category].map(({ type, details }) => (
                                        <div
                                            key={type}
                                            onClick={() => createNode(type)}
                                            className="node-item"
                                            onMouseEnter={() => setHoveredNode(type)}
                                            onMouseLeave={() => setHoveredNode(null)}
                                            style={{
                                                border: `1px solid ${details.color}${type === hoveredNode ? '22' : '11'}`
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
                            </div>
                        );
                    })
                ) : (
                    <div className="empty-state">
                        No nodes match your search
                    </div>
                )}
            </div>
        </div>
    );
};

export default NodeTypesPanel;