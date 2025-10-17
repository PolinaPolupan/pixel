import React, { useCallback, useEffect, useState, useRef } from 'react';
import { useNodesApi } from '../../hooks/useNodesApi.js';
import {useNotification} from "../../services/contexts/NotificationContext.jsx";

const ContextMenu = ({
                         onClose,
                         position = { x: 0, y: 0 },
                         createNode
                     }) => {
    const { nodesGroupedByCategory, isLoading, error } = useNodesApi();
    const { setError } = useNotification();
    const [expandedCategory, setExpandedCategory] = useState(null);
    const [subMenuPosition, setSubMenuPosition] = useState({ top: 0, height: 0 });
    const categoryRefs = useRef({});
    const mainMenuRef = useRef(null);

    useEffect(() => {
        if (error) {
            setError(`Error loading node types: ${error}`);
        }
    }, [error, setError]);

    const handleMouseEnter = (category) => {
        const element = categoryRefs.current[category];
        if (element) {
            const rect = element.getBoundingClientRect();
            const mainMenuRect = mainMenuRef.current.getBoundingClientRect();

            setSubMenuPosition({
                top: rect.top - mainMenuRect.top,
                height: rect.height
            });
        }
        setExpandedCategory(category);
    };

    const handleMouseLeave = (e) => {
        // Check if we're leaving to the submenu or outside entirely
        const relatedTarget = e.relatedTarget;
        if (!relatedTarget ||
            (relatedTarget.id !== 'submenu' &&
                !relatedTarget.closest('.main-context-menu') &&
                !relatedTarget.closest('.context-submenu'))) {
            setExpandedCategory(null);
        }
    };

    const handleSubMenuMouseLeave = (e) => {
        const relatedTarget = e.relatedTarget;
        if (!relatedTarget ||
            (!relatedTarget.closest('.main-context-menu') &&
                !relatedTarget.closest('.context-submenu'))) {
            setExpandedCategory(null);
        }
    };

    const handleClick = useCallback((type) => {
        createNode(type, position);
        onClose();
    }, [createNode, position, onClose]);

    useEffect(() => {
        const handleClickOutside = () => onClose();
        document.addEventListener('click', handleClickOutside);
        return () => document.removeEventListener('click', handleClickOutside);
    }, [onClose]);

    const renderIcon = (IconComponent) => {
        if (!IconComponent) return null;
        return <IconComponent theme={{ colors: { onSurface: 'white' } }} />;
    };

    if (isLoading) {
        return (
            <div
                className="context-menu"
                style={{
                    position: 'absolute',
                    top: position.y,
                    left: position.x,
                    zIndex: 10,
                    background: 'rgba(35, 35, 40, 0.95)',
                    borderRadius: '8px',
                    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.3)',
                    border: '1px solid rgba(255, 255, 255, 0.1)',
                    padding: '10px',
                    minWidth: '150px',
                    color: 'white',
                    fontSize: '13px'
                }}
            >
                Loading nodes...
            </div>
        );
    }

    if (error) {
        return null;
    }

    return (
        <div style={{ position: 'absolute', top: position.y, left: position.x, zIndex: 10 }}>
            <div
                className="main-context-menu"
                ref={mainMenuRef}
                style={{
                    background: 'rgba(35,35,40,0.49)',
                    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.3)',
                    minWidth: '180px',
                    maxHeight: '70vh',
                    overflowY: 'auto',
                    padding: '5px 0',
                }}
                onClick={(e) => e.stopPropagation()}
                onMouseLeave={handleMouseLeave}
            >
                {Object.keys(nodesGroupedByCategory).map((category) => (
                    <div
                        key={category}
                        ref={el => categoryRefs.current[category] = el}
                        onMouseEnter={(e) => handleMouseEnter(category, e)}
                        style={{
                            padding: '6px 10px',
                            color: 'rgba(255, 255, 255, 0.9)',
                            fontSize: '12px',
                            backgroundColor: expandedCategory === category
                                ? 'rgba(255, 255, 255, 0.02)'
                                : 'rgba(255, 255, 255, 0)',
                            borderTop: '1px solid rgba(255, 255, 255, 0.05)',
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            cursor: 'default',
                            transition: 'background-color 0.15s ease'
                        }}
                    >
                        <span>{category}</span>
                        <span style={{ fontSize: '10px' }}>►</span>
                    </div>
                ))}
            </div>

            {expandedCategory && (
                <div
                    id="submenu"
                    className="context-submenu"
                    style={{
                        position: 'absolute',
                        top: Math.max(0, subMenuPosition.top - 5),
                        left: '100%',
                        marginLeft: '2px',
                        background: 'rgba(35,35,40,0.49)',
                        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.3)',
                        border: '1px solid rgba(255, 255, 255, 0.1)',
                        padding: '5px 0',
                        minWidth: '200px',
                        maxHeight: '350px',
                        overflowY: 'auto',
                        zIndex: 11,
                        opacity: 1,
                        transition: 'opacity 0.15s ease',
                    }}
                    onMouseLeave={handleSubMenuMouseLeave}
                >
                    {nodesGroupedByCategory[expandedCategory].map(({ type, display }) => (
                        <div
                            key={type}
                            onClick={() => handleClick(type)}
                            style={{
                                padding: '6px 10px',
                                cursor: 'pointer',
                                transition: 'background 0.2s',
                                color: 'white',
                                fontSize: '13px',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '8px'
                            }}
                            onMouseOver={(e) => {
                                e.currentTarget.style.background = 'rgba(255, 255, 255, 0.1)';
                            }}
                            onMouseOut={(e) => {
                                e.currentTarget.style.background = 'transparent';
                            }}
                        >
              <span style={{
                  display: 'flex',
                  alignItems: 'center',
                  color: display.color || 'white',
              }}>
                {renderIcon(display.icon)}
              </span>
                            <span>{display.name}</span>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default ContextMenu;