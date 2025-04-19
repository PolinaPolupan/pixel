import React, { useCallback, useEffect } from 'react';
import { useReactFlow } from '@xyflow/react';
import { nodeTypeDetails } from '../utils/nodeTypes';


const ContextMenu = ({ 
  onClick, 
  onClose, 
  position = { x: 0, y: 0 }, 
  createNode
}) => {
  const handleClick = useCallback((type) => {
    createNode(type, position);
    onClose();
  }, [createNode, position, onClose]);
  
  // Close menu if user clicks outside
  useEffect(() => {
    const handleClickOutside = () => onClose();
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, [onClose]);
  
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
        padding: '5px 0',
        minWidth: '150px',
      }}
    >
      <div style={{ 
        borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
        padding: '5px 10px',
        color: 'rgba(255, 255, 255, 0.5)',
        fontSize: '12px',
        fontWeight: 'bold'
      }}>
        Add Node
      </div>
      
      {Object.keys(nodeTypeDetails).map((type) => (
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
          <span style={{ fontSize: '14px' }}>{nodeTypeDetails[type].icon}</span>
          {type}
        </div>
      ))}
    </div>
  );
};

export default ContextMenu;