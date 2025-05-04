import {
    useReactFlow,
} from '@xyflow/react';

import LabeledHandle from '../handles/LabeledHandle';
import NodeHeader from '../NodeHeader';
import InputHandle from '../handles/InputHandle';

export default function OutputFile({ id, data }) {
    const { updateNodeData } = useReactFlow();

    return (
        <div style={{
            minWidth: '100px'
        }}>
            <NodeHeader title={"Output File"}/>
            <InputHandle
                id={id}
                data={data}
                handleId="filename"
                handleLabel="Filename"
                type = "text"
                parameterType="STRING"
            />
            <InputHandle
                id={id}
                data={data}
                handleId="content"
                handleLabel="Content"
                type = "text"
                parameterType="STRING"
            />
        </div>
    );
}