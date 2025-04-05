// EnterHandle.jsx
import React from 'react';
import { useReactFlow } from '@xyflow/react';
import LabeledHandle from './LabeledHandle';

const InputHandle = ({ 
  id, 
  data, 
  handleId, 
  handleLabel, 
  type = 'number' 
}) => {
  const { updateNodeData } = useReactFlow();

  // Determine if the node is connected
  const isConnected = data.isConnected || false;

  // Handle input change when not connected
  const handleInputChange = (evt) => {
    if (!isConnected) {
      const value = type === 'number' ? evt.target.valueAsNumber || evt.target.value : evt.target.value;
      updateNodeData(id, { text: value });
    }
  };

  return (
    <div>
      <LabeledHandle
        label={handleLabel}
        type="target"
        position="left"
        id={handleId}
      />
      <input
        type={type}
        onChange={handleInputChange}
        value={data.text || ''} 
        disabled={isConnected}
        style={{
          display: 'block',
          width: '100px',
          padding: '5px',
          boxSizing: 'border-box',
          border: '1px solid #ccc',
          borderRadius: '3px',
          background: isConnected ? '#e0e0e0' : '#fff',
          color: isConnected ? '#888' : '#000'
        }}
      />
    </div>
  );
};

export default InputHandle;