import NodeHeader from '../../ui/NodeHeader.jsx';
import InputHandle from '../../handles/InputHandle.jsx';

export default function OutputFile({ id, data }) {
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