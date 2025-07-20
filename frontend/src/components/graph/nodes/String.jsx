import LabeledHandle from '../../handles/LabeledHandle.jsx';
import NodeHeader from '../../ui/NodeHeader.jsx';
import InputHandle from '../../handles/InputHandle.jsx';
 
export default function String({ id, data }) {
  return (
    <div style={{ 
      minWidth: '100px'
    }}>
      <NodeHeader title={"String"}/>
      <InputHandle 
        id={id}
        data={data}
        handleId="value"
        handleLabel="Value"
        parameterType="STRING"
        type = "text"
      />
      <LabeledHandle 
        label="Value"
        type="source"
        position="right" 
        id="value"
        connectionCount="10"
        parameterType="STRING"
      />
    </div>
  );
}