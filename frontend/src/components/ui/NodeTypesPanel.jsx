import React, { useCallback, useState, useEffect } from 'react';
import { useReactFlow, useStoreApi } from '@xyflow/react';
import { useNodesApi } from '../../hooks/useNodesApi.js';
import '../../styles/App.css';
import {useCreateNode} from "../../hooks/useCreateNode.js";

const NodeTypesPanel = () => {
    const { getNodes, addNodes, screenToFlowPosition } = useReactFlow();
    const store = useStoreApi();
    const { createNode } = useCreateNode();

    const {
        nodesGroupedByCategory,
        isLoading,
        error
    } = useNodesApi();

    // Track expanded categories
    const [expandedCategories, setExpandedCategories] = useState({});

    // Initialize expanded state for categories
    useEffect(() => {
        if (Object.keys(nodesGroupedByCategory).length > 0) {
            const initialExpanded = Object.fromEntries(
                Object.keys(nodesGroupedByCategory).map(category => [category, false])
            );
            setExpandedCategories(initialExpanded);
        }
    }, [nodesGroupedByCategory]);

    const toggleCategory = (category) => {
        setExpandedCategories(prev => ({
            ...prev,
            [category]: !prev[category]
        }));
    };

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

        return {
            position: {
                x: center.x - nodeDimensions.width / 2,
                y: center.y - nodeDimensions.height / 2,
            },
            ...nodeDimensions
        };
    }, [screenToFlowPosition, store]);

    if (isLoading) {
        return <div className="node-types-panel">Loading node types...</div>;
    }

    if (error) {
        return (
            <div className="node-types-panel" style={{ color: '#ff6b6b' }}>
                Error loading node types: {error}
            </div>
        );
    }

    const visibleCategories = Object.keys(nodesGroupedByCategory).sort();

    return (
        <div className="node-types-panel">
            {visibleCategories.map(category => {
                const firstNodeColor =
                    nodesGroupedByCategory[category][0]?.display?.color || '#ffffff';

                return (
                    <div key={category} style={{ marginBottom: '8px' }}>
                        {/* Category Header */}
                        <div
                            onClick={() => toggleCategory(category)}
                            className="node-category"
                            style={{
                                background: `linear-gradient(45deg, ${firstNodeColor}11, rgba(255,255,255,0))`
                            }}
                        >
                            <span style={{ color: "rgba(255,255,255,0.8)" }}>
                                {category}
                            </span>
                            <span
                                style={{ color: "rgba(255,255,255,0.5)" }}
                                className={`arrow-icon ${expandedCategories[category] ? 'expanded' : ''}`}
                            >
                                {expandedCategories[category] ? '▼' : '▶'}
                            </span>
                        </div>

                        {/* Node items */}
                        <div className={`node-items-container ${expandedCategories[category] ? 'expanded' : ''}`}>
                            {nodesGroupedByCategory[category].map(({ type, display }) => {
                                const IconComponent = display.icon;
                                return (
                                    <div
                                        key={type}
                                        onClick={() => createNode(type, getViewportCenterNode().position)}
                                        className="node-item"
                                        style={{
                                            border: `1px solid ${display.color}22`
                                        }}
                                    >
                                        {IconComponent && (
                                            <IconComponent
                                                style={{ fontSize: '16px', color: display.color }}
                                            />
                                        )}
                                        <div>
                                            <div style={{ fontWeight: 'bold', marginBottom: '2px' }}>
                                                {display.name}
                                            </div>
                                            <div style={{ fontSize: '11px', opacity: '0.7' }}>
                                                {display.description}
                                            </div>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                );
            })}
        </div>
    );
};

export default NodeTypesPanel;
