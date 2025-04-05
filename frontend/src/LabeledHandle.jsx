// LabeledHandle.jsx
import { Handle, Position } from '@xyflow/react';
import { memo } from 'react';


function LabeledHandle({
  label,
  type = 'source',
  position = Position.Right,
  id,
  style = {},
  labelStyle = {},
  containerStyle = {},
  color = 'rgba(0, 0, 0, 0.9)',
  fontSize = '10px'
}) {
  // Determine the default label position based on handle position
  const defaultLabelStyle = {
    position: 'absolute',
    fontSize: fontSize,
    lineHeight: '1',
    top: '50%',
    transform: 'translateY(-50%)',
    color: color
  };
  
  // Adjust label position based on handle position
  if (position === Position.Right) {
    defaultLabelStyle.right = '12px';
  } else if (position === Position.Left) {
    defaultLabelStyle.left = '12px';
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
    height: '20px',
    marginTop: '5px'
  };

  const defaultHandleStyle = {
    top: '50%',
    transform: 'translateY(-50%)'
  };

  return (
    <div style={{ ...defaultContainerStyle, ...containerStyle }}>
      <div style={{ ...defaultLabelStyle, ...labelStyle }}>
        {label}
      </div>
      <Handle
        type={type}
        position={position}
        id={id}
        style={{ ...defaultHandleStyle, ...style }}
      />
    </div>
  );
}

export default memo(LabeledHandle);