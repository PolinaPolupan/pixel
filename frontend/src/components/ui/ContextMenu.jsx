import React, { useCallback, useEffect, useState, useRef } from 'react';
import { useNodesApi } from '../../hooks/useNodesApi.js';
import { useNotification } from "../../services/contexts/NotificationContext.jsx";
import './ContextMenu.css';

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
            const mainMenuRect = mainMenuRef.current. getBoundingClientRect();

            setSubMenuPosition({
                top:  rect.top - mainMenuRect. top,
                height: rect. height
            });
        }
        setExpandedCategory(category);
    };

    const handleMouseLeave = (e) => {
        const relatedTarget = e.relatedTarget;
        if (! relatedTarget ||
            (relatedTarget.id !== 'submenu' &&
                ! relatedTarget.closest('.main-context-menu') &&
                !relatedTarget. closest('.context-submenu'))) {
            setExpandedCategory(null);
        }
    };

    const handleSubMenuMouseLeave = (e) => {
        const relatedTarget = e. relatedTarget;
        if (! relatedTarget ||
            (! relatedTarget.closest('.main-context-menu') &&
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
        if (! IconComponent) return null;
        return <IconComponent theme={{ colors: { onSurface: 'white' } }} />;
    };

    if (isLoading) {
        return (
            <div
                className="context-menu-wrapper"
                style={{ top: position.y, left: position.x }}
            >
                <div className="context-menu">
                    Loading nodes...
                </div>
            </div>
        );
    }

    if (error) {
        return null;
    }

    return (
        <div className="context-menu-wrapper" style={{ top: position.y, left: position.x }}>
            <div
                className="main-context-menu"
                ref={mainMenuRef}
                onClick={(e) => e.stopPropagation()}
                onMouseLeave={handleMouseLeave}
            >
                {Object.keys(nodesGroupedByCategory).map((category) => (
                    <div
                        key={category}
                        ref={el => categoryRefs.current[category] = el}
                        onMouseEnter={() => handleMouseEnter(category)}
                        className={`context-menu-category ${expandedCategory === category ? 'active' : ''}`}
                    >
                        <span>{category}</span>
                        <span className="context-menu-category-arrow">►</span>
                    </div>
                ))}
            </div>

            {expandedCategory && (
                <div
                    id="submenu"
                    className="context-submenu"
                    style={{
                        top:  Math.max(0, subMenuPosition.top - 5),
                    }}
                    onMouseLeave={handleSubMenuMouseLeave}
                >
                    {nodesGroupedByCategory[expandedCategory].map(({ type, display }) => (
                        <div
                            key={type}
                            onClick={() => handleClick(type)}
                            className="context-menu-item"
                        >
                            <span className="context-menu-item-icon" style={{ color: display.color || 'white' }}>
                                {renderIcon(display.icon)}
                            </span>
                            <span className="context-menu-item-name">{display. name}</span>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default ContextMenu;