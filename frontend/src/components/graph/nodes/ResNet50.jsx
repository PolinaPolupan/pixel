import LabeledHandle from '../../handles/LabeledHandle.jsx';
import NodeHeader from '../../ui/NodeHeader.jsx';

export default function ResNet50() {
    return (
        <div style={{
            minWidth: '100px'
        }}>
            <NodeHeader title={"ResNet50"}/>
            <LabeledHandle
                label="Files"
                type="target"
                position="left"
                id="files"
                parameterType="FILENAMES_ARRAY"
            />
            <LabeledHandle
                label="json"
                type="source"
                position="right"
                id="json"
                connectionCount="10"
                parameterType="STRING"
            />
        </div>
    );
}