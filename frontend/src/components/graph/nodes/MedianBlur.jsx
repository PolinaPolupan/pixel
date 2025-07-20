import LabeledHandle from '../../handles/LabeledHandle.jsx';
import NodeHeader from '../../ui/NodeHeader.jsx';
import InputHandle from '../../handles/InputHandle.jsx';

export default function MedianBlur({ id, data }) {
    return (
        <div style={{
            minWidth: '100px'
        }}>
            <NodeHeader title={"Median blur"}/>
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
                handleId="ksize"
                handleLabel="ksize"
                parameterType="INT"
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
