import React, { useCallback, useState, useEffect } from 'react';
import { useReactFlow, useStoreApi } from '@xyflow/react';
import { useNodesApi } from '../../hooks/useNodesApi.js';
import { useCreateNode } from "../../hooks/useCreateNode.js";
import './NodeTypesPanel.css';

const NodeTypesPanel = () => {
    const { screenToFlowPosition } = useReactFlow();
    const store = useStoreApi();
    const { createNode } = useCreateNode();
    const { nodesGroupedByCategory, isLoading, error } = useNodesApi();
    const [expandedCategories, setExpandedCategories] = useState({});

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
        const boundingRect = domNode?. getBoundingClientRect();

        if (!boundingRect) {
            return { position: { x:  100, y: 100 }, width: 150, height:  60 };
        }

        const center = screenToFlowPosition({
            x: boundingRect.x + boundingRect. width / 2,
            y: boundingRect.y + boundingRect.height / 2,
        });

        return {
            position: {
                x: center.x - 75,
                y: center.y - 30,
            },
            width: 150,
            height: 60
        };
    }, [screenToFlowPosition, store]);

    if (isLoading) {
        return <div className="node-types-panel node-types-panel-loading">Loading... </div>;
    }

    if (error) {
        return (
            <div className="node-types-panel node-types-panel-error">
                Error:  {error}
            </div>
        );
    }

    const visibleCategories = Object. keys(nodesGroupedByCategory).sort();

    return (
        <div className="node-types-panel">
            {visibleCategories.map(category => (
                <div key={category}>
                    <div
                        onClick={() => toggleCategory(category)}
                        className="node-category"
                    >
                        <span className="node-category-title">
                            {category}
                        </span>
                        <span className={`arrow-icon ${expandedCategories[category] ? 'expanded' : ''}`}>
                            ▶
                        </span>
                    </div>

                    <div className={`node-items-container ${expandedCategories[category] ? 'expanded' : ''}`}>
                        {nodesGroupedByCategory[category].map(({ type, display }) => {
                            const IconComponent = display.icon;
                            return (
                                <div
                                    key={type}
                                    onClick={() => createNode(type, getViewportCenterNode().position)}
                                    className="node-item"
                                    style={{ borderLeftColor: display.color }}
                                >
                                    {IconComponent && (
                                        <span className="node-item-icon" style={{ color: display.color }}>
                                            <IconComponent />
                                        </span>
                                    )}
                                    <div className="node-item-content">
                                        <div className="node-item-name">
                                            {display. name}
                                        </div>
                                        {display.description && (
                                            <div className="node-item-description" title={display.description}>
                                                {display.description}
                                            </div>
                                        )}
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </div>
            ))}
        </div>
    );
};

export default NodeTypesPanel;