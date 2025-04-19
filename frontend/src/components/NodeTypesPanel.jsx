import React from 'react';
import { useReactFlow } from '@xyflow/react';
import { getParameterColor } from '../utils/parameterColors';
import { nodeTypeDetails } from '../utils/nodeTypes';
import { defaultData } from '../utils/nodeTypes';

const NodeTypesPanel = () => {
  const { getNodes, addNodes } = useReactFlow();
  
  const createNode = (type, position) => {
    // Get highest node ID to ensure unique IDs
    const nodeIds = getNodes().map(node => parseInt(node.id));
    const newId = (Math.max(...nodeIds, 0) + 1).toString();
    
    // Create new node
    const newNode = {
      id: newId,
      type,
      position,
      data: defaultData[type] || {}
    };
    
    addNodes(newNode);
  };
  
  return (
    <div style={{
      width: '100%',
      height: '100%',
      background: 'rgba(40, 40, 40, 0.95)',
      borderRadius: '8px',
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
          gap: '8px',
          marginRight: '12px'
        }}>
          {Object.keys(nodeTypeDetails).map((type) => (
            <div
              key={type}
              onClick={() => createNode(type, { x: 100, y: 100 })}
              style={{
                background: `linear-gradient(45deg, ${nodeTypeDetails[type].color}22, ${nodeTypeDetails[type].color}11)`,
                border: `1px solid ${nodeTypeDetails[type].color}33`,
                borderRadius: '6px',
                padding: '8px 10px',
                cursor: 'pointer',
                transition: 'all 0.2s ease',
                color: '#ffffff',
                fontSize: '13px',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}
              onMouseOver={(e) => {
                e.currentTarget.style.background = `linear-gradient(45deg, ${nodeTypeDetails[type].color}44, ${nodeTypeDetails[type].color}22)`;
                e.currentTarget.style.transform = 'translateY(-1px)';
                e.currentTarget.style.boxShadow = '0 4px 8px rgba(0,0,0,0.15)';
              }}
              onMouseOut={(e) => {
                e.currentTarget.style.background = `linear-gradient(45deg, ${nodeTypeDetails[type].color}22, ${nodeTypeDetails[type].color}11)`;
                e.currentTarget.style.transform = 'translateY(0)';
                e.currentTarget.style.boxShadow = 'none';
              }}
            >
              <span style={{ fontSize: '16px' }}>{nodeTypeDetails[type].icon}</span>
              <div>
                <div style={{ fontWeight: 'bold', marginBottom: '2px' }}>{type}</div>
                <div style={{ fontSize: '11px', opacity: '0.7' }}>{nodeTypeDetails[type].description}</div>
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