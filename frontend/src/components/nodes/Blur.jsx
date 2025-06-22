import {
    useReactFlow,
} from '@xyflow/react';

import LabeledHandle from '../handles/LabeledHandle';
import NodeHeader from '../NodeHeader';
import InputHandle from '../handles/InputHandle';

export default function Blur({ id, data }) {
    const { updateNodeData } = useReactFlow();

    return (
        <div style={{
            minWidth: '100px'
        }}>
            <NodeHeader title={"Blur"}/>
            <LabeledHandle
                label="Files"
                type="target"
                position="left"
                id="files"
                parameterType="FILENAMES_ARRAY"
            />
            <LabeledHandle
                label="ksize"
                type="target"
                position="left"
                id="ksize"
                parameterType="VECTOR2D"
            />
            <LabeledHandle
                label="Files"
                type="source"
                position="right"
                id="files"
                connectionCount="10"
                parameterType="FILENAMES_ARRAY"
            />
        </div>
    );
}
