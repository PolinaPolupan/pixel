import React, { useCallback } from 'react';
import { useReactFlow, useStoreApi } from '@xyflow/react';
import { nodesConfig } from '../utils/NodesConfig';

const NodeTypesPanel = () => {
  const { getNodes, addNodes, screenToFlowPosition } = useReactFlow();
  const store = useStoreApi();

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

    // Estimate node dimensions (based on String.jsx: 150px wide, ~60px tall)
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
    const { position, width, height } = getViewportCenterNode(type);

    const newNode = {
      id: newId,
      type,
      position,
      data: nodesConfig[type].defaultData
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
      <div style={{
        flex: 1,
        overflowY: 'auto',
        marginBottom: '12px',
        marginRight: '12px'
      }}>
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          gap: '8px'
        }}>
          {Object.keys(nodesConfig).map((type) => (
            <div
              key={type}
              onClick={() => createNode(type)}
              style={{
                background: `linear-gradient(45deg, ${nodesConfig[type].display.color}11, ${nodesConfig[type].display.color}5)`,
                border: `1px solid ${nodesConfig[type].display.color}22`,
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
                e.currentTarget.style.border = `1px solid ${nodesConfig[type].display.color}44`;
                e.currentTarget.style.transform = 'translateY(-1px)';
                e.currentTarget.style.boxShadow = '0 4px 8px rgba(0,0,0,0.15)';
              }}
              onMouseOut={(e) => {
                e.currentTarget.style.border = `1px solid ${nodesConfig[type].display.color}22`;
                e.currentTarget.style.transform = 'translateY(0)';
                e.currentTarget.style.boxShadow = 'none';
              }}
            >
              {nodesConfig[type].display.icon && renderIcon(nodesConfig[type].display.icon, type)}
              <div>
                <div style={{ fontWeight: 'bold', marginBottom: '2px' }}>{type}</div>
                <div style={{ fontSize: '11px', opacity: '0.7' }}>{nodesConfig[type].display.description}</div>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div style={{
        fontSize: '11px',
        color: 'rgba(255, 255, 255, 0.5)',
        textAlign: 'center',
        paddingTop: '8px'
      }}>
        Tip: Right-click anywhere to add a node at cursor position
      </div>
    </div>
  );
};

export default NodeTypesPanel;