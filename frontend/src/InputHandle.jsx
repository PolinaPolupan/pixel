import React from 'react';
import { useReactFlow, useNodeConnections } from '@xyflow/react';
import LabeledHandle from './LabeledHandle';
import { getParameterColor } from './parameterColors';

const InputHandle = ({ 
  id, 
  data, 
  handleId, 
  handleLabel, 
  type = 'number', 
  parameterType = 'FLOAT', // New prop for parameter type
}) => {
  const { updateNodeData } = useReactFlow();

  const connections = useNodeConnections({
    handleId: handleId,
    handleType: 'target',
  });

  const isConnected = connections.length > 0;

  const handleInputChange = (evt) => {
    if (!isConnected) {
      const value = type === 'number' ? evt.target.valueAsNumber || evt.target.value : evt.target.value;
      updateNodeData(id, { [handleId]: value });
    }
  };

  const inputStyle = {
    display: 'block',
    width: '100px',
    height: '15px',
    padding: '5px',
    boxSizing: 'border-box',
    margin: 'auto 5px 5px',
    border: `1px solid ${parameterType ? getParameterColor(parameterType) : '#ccc'}`, // Colored border
    borderRadius: '3px',
    background: isConnected ? '#e0e0e0' : '#fff',
    color: isConnected ? '#888' : '#000',
    fontSize: '8px',
  };

  return (
    <div>
      <LabeledHandle
        label={handleLabel}
        type="target"
        position="left"
        id={handleId}
        parameterType={parameterType} // Pass to LabeledHandle
      />
      <input
        type={type}
        onChange={handleInputChange}
        value={data[handleId] || ''}
        disabled={isConnected}
        style={inputStyle}
      />
    </div>
  );
};

export default InputHandle;