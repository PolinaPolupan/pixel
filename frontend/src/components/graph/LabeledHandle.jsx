import React, { memo } from 'react';
import { Handle, Position, useNodeConnections } from '@xyflow/react';
import { getParameterColor } from "../../services/NodesConfig.jsx";
import './Node.css';

const LabeledHandle = (props) => {
    const {
        label,
        type = 'source',
        position = Position. Right,
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

    const connections = useNodeConnections({
        handleId: id,
        handleType: type,
    });

    const getPositionClass = () => {
        switch (position) {
            case Position.Right:
                return 'labeled-handle-label-right';
            case Position.Left:
                return 'labeled-handle-label-left';
            case Position.Top:
                return 'labeled-handle-label-top';
            case Position.Bottom:
                return 'labeled-handle-label-bottom';
            default:
                return 'labeled-handle-label-right';
        }
    };

    const handleStyle = {
        background: parameterType ? getParameterColor(parameterType) : '#cccccc',
        ... style
    };

    const computedLabelStyle = {
        color,
        fontSize,
        ...labelStyle
    };

    return (
        <div
            className="labeled-handle-container"
            style={containerStyle}
        >
            <div
                className={`labeled-handle-label ${getPositionClass()}`}
                style={computedLabelStyle}
            >
                {label}
            </div>
            <Handle
                type={type}
                position={position}
                id={id}
                className="labeled-handle"
                style={handleStyle}
                isConnectable={connections.length < connectionCount}
                {...rest}
            />
        </div>
    );
};

export default memo(LabeledHandle);