import {
    useReactFlow,
} from '@xyflow/react';

import LabeledHandle from '../handles/LabeledHandle';
import NodeHeader from '../NodeHeader';
import InputHandle from '../handles/InputHandle';

export default function Vector2D({ id, data }) {
    const { updateNodeData } = useReactFlow();

    return (
        <div style={{
            minWidth: '100px'
        }}>
            <NodeHeader title={"Vector2D"}/>
            <InputHandle
                id={id}
                data={data}
                handleId="x"
                handleLabel="x"
                parameterType="DOUBLE"
            />
            <InputHandle
                id={id}
                data={data}
                handleId="y"
                handleLabel="y"
                parameterType="DOUBLE"
            />
            <LabeledHandle
                label="Vector2D"
                type="source"
                position="right"
                id="vector2D"
                connectionCount="10"
                parameterType="VECTOR2D"
            />
        </div>
    );
}