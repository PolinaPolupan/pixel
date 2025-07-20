import LabeledHandle from '../../handles/LabeledHandle.jsx';
import NodeHeader from '../../ui/NodeHeader.jsx';
import InputHandle from '../../handles/InputHandle.jsx';

export default function Vector2D({ id, data }) {
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