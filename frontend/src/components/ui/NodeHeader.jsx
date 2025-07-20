import React from 'react';

const NodeHeader = ({ title, ...props }) => {
  const headerStyle = {
    fontWeight: 'bold',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    margin: '5px'
  };

  return (
    <div style={headerStyle} {...props}>
      <span>{title}</span>
    </div>
  );
};

export default NodeHeader;