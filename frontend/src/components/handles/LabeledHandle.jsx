import React from 'react';
import { Handle, Position, useNodeConnections } from '@xyflow/react';
import { memo } from 'react';
import { getParameterColor } from '../../utils/parameterColors';

const LabeledHandle = (props) => {
  const {
    label,
    type = 'source',
    position = Position.Right,
    id,
    style = {},
    labelStyle = {},
    containerStyle = {},
    color = 'rgba(255, 255, 255, 0.6)',
    fontSize = '8px',
    connectionCount = 1,
    parameterType,
    ...rest
  } = props;

  const defaultLabelStyle = {
    position: 'absolute',
    fontSize: fontSize,
    top: '50%',
    transform: 'translateY(-50%)',
    fontWeight: '600',
    color: color
  };

  if (position === Position.Right) {
    defaultLabelStyle.right = '8px';
  } else if (position === Position.Left) {
    defaultLabelStyle.left = '8px';
  } else if (position === Position.Top) {
    defaultLabelStyle.top = 'auto';
    defaultLabelStyle.bottom = '15px';
    defaultLabelStyle.left = '50%';
    defaultLabelStyle.transform = 'translateX(-50%)';
  } else if (position === Position.Bottom) {
    defaultLabelStyle.top = '15px';
    defaultLabelStyle.left = '50%';
    defaultLabelStyle.transform = 'translateX(-50%)';
  }

  const defaultContainerStyle = {
    position: 'relative',
    height: '15px',
  };

  const connections = useNodeConnections({
    handleId: id,
    handleType: type,
  });

  // Default handle style with parameter type color
  const defaultHandleStyle = {
    width: '10px',
    height: '10px',
    background: parameterType ? getParameterColor(parameterType) : '#cccccc',
  };

  return (
    <div style={{ ...defaultContainerStyle, ...containerStyle }}>
      <div style={{ ...defaultLabelStyle, ...labelStyle }}>{label}</div>
      <Handle
        type={type}
        position={position}
        id={id}
        style={{ ...defaultHandleStyle, ...style }} // Merge default with custom styles
        isConnectable={connections.length < connectionCount}
        {...rest}
      />
    </div>
  );
};

export default memo(LabeledHandle);