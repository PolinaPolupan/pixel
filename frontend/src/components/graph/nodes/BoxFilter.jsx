import LabeledHandle from '../../handles/LabeledHandle.jsx';
import NodeHeader from '../../ui/NodeHeader.jsx';
import InputHandle from '../../handles/InputHandle.jsx';

export default function BoxFilter({ id, data }) {
    return (
        <div style={{
            minWidth: '100px'
        }}>
            <NodeHeader title={"Box filter"}/>
            <LabeledHandle
                label="Files"
                type="target"
                position="left"
                id="files"
                parameterType="FILENAMES_ARRAY"
            />
            <InputHandle
                id={id}
                data={data}
                handleId="ddepth"
                handleLabel="ddepth"
                parameterType="INT"
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
