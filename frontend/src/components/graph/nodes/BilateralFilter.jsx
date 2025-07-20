import LabeledHandle from '../../handles/LabeledHandle.jsx';
import NodeHeader from '../../ui/NodeHeader.jsx';
import InputHandle from '../../handles/InputHandle.jsx';

export default function BilateralFilter({ id, data }) {
    return (
        <div style={{
            minWidth: '100px'
        }}>
            <NodeHeader title={"Bilateral filter"}/>
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
                handleId="d"
                handleLabel="d"
                parameterType="INT"
            />
            <InputHandle
                id={id}
                data={data}
                handleId="sigmaColor"
                handleLabel="Sigma color"
                parameterType="DOUBLE"
            />
            <InputHandle
                id={id}
                data={data}
                handleId="sigmaSpace"
                handleLabel="Sigma space"
                parameterType="DOUBLE"
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
