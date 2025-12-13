import { useReactFlow, useNodeConnections } from '@xyflow/react';
import LabeledHandle from './LabeledHandle.jsx';
import { getParameterColor } from "../../services/NodesConfig.jsx";
import './Node.css';

const InputHandle = ({
                         id,
                         data,
                         handleId,
                         handleLabel,
                         parameterType
                     }) => {
    const { updateNodeData } = useReactFlow();

    const connections = useNodeConnections({
        handleId: handleId,
        handleType: 'target',
    });

    const isConnected = connections.length > 0;
    const inputValue = data[handleId] ?? '';

    const handleInputChange = (evt) => {
        if (!isConnected) {
            updateNodeData(id, { [handleId]: evt. target.value });
        }
    };

    const getBorderColor = () => {
        if (! parameterType) {
            return 'rgba(255, 255, 255, 0.2)';
        }
        const color = getParameterColor(parameterType);
        const rgb = hexToRgb(color);
        return `rgba(${rgb}, 0.3)`;
    };

    return (
        <div className="input-handle-container">
            <LabeledHandle
                label={handleLabel}
                type="target"
                position="left"
                id={handleId}
                parameterType={parameterType}
            />
            <input
                onChange={handleInputChange}
                value={inputValue}
                disabled={isConnected}
                className="input-handle-input"
                style={{ borderColor: getBorderColor() }}
            />
        </div>
    );
};

const hexToRgb = (hex) => {
    const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result
        ? `${parseInt(result[1], 16)}, ${parseInt(result[2], 16)}, ${parseInt(result[3], 16)}`
        : '255, 255, 255';
};

export default InputHandle;