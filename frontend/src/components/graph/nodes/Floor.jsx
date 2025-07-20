import LabeledHandle from '../../handles/LabeledHandle.jsx';
import NodeHeader from '../../ui/NodeHeader.jsx';
import InputHandle from '../../handles/InputHandle.jsx';
 
export default function Floor({ id, data }) {
 return (
    <div style={{ 
      minWidth: '100px'
    }}>
      <NodeHeader title={"Floor"}/>
      <InputHandle 
        id={id}
        data={data}
        handleId="number"
        handleLabel="Number"
        parameterType="DOUBLE"
      />
      <LabeledHandle 
        label="Number"
        type="source"
        position="right" 
        id="number"
        connectionCount="10"
        parameterType="DOUBLE"
      />
    </div>
  );
}