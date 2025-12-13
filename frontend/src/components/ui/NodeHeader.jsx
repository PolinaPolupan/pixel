import React from 'react';
import './NodeHeader.css';

const NodeHeader = ({ title, ... props }) => {
    return (
        <div className="node-header" {...props}>
            <span>{title}</span>
        </div>
    );
};

export default NodeHeader;