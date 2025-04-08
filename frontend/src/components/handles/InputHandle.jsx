import React from 'react';
import { useReactFlow, useNodeConnections } from '@xyflow/react';
import LabeledHandle from './LabeledHandle';
import { getParameterColor } from '../../utils/parameterColors';

const InputHandle = ({ 
  id, 
  data, 
  handleId, 
  handleLabel, 
  type = 'number', 
  parameterType,
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

  const inputClass = `input-handle-${id}-${handleId}`; // Unique class per instance

  const inputStyle = {
    display: 'block',
    width: '100px',
    height: '15px',
    padding: '5px',
    boxSizing: 'border-box',
    margin: 'auto 5px 5px',
    border: `1px solid ${parameterType ? `rgba(${hexToRgb(getParameterColor(parameterType))}, 0.3)` : 'rgba(255, 255, 255, 0.2)'}`,
    borderRadius: '3px',
    background: isConnected ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0)',
    color: isConnected ? 'rgba(255, 255, 255, 0.5)' : 'inherit',
    fontSize: '8px',
    outline: 'none',
  };

  return (
    <div>
      <style>{`
        .${inputClass} {
          -moz-appearance: textfield; /* Optional: hide arrows in Firefox */
        }
      `}</style>
      <LabeledHandle
        label={handleLabel}
        type="target"
        position="left"
        id={handleId}
        parameterType={parameterType}
      />
      <input
        type={type}
        onChange={handleInputChange}
        value={data[handleId] ?? ''}
        disabled={isConnected}
        className={inputClass}
        style={inputStyle}
      />
    </div>
  );
};

const hexToRgb = (hex) => {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  return result ? `${parseInt(result[1], 16)}, ${parseInt(result[2], 16)}, ${parseInt(result[3], 16)}` : '255, 255, 255';
};

export default InputHandle;