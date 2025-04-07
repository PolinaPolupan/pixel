import {
  useReactFlow,
} from '@xyflow/react';

import LabeledHandle from '../handles/LabeledHandle';
import NodeHeader from '../NodeHeader';
import InputHandle from '../handles/InputHandle';
 
function FloorNode({ id, data }) {
  const { updateNodeData } = useReactFlow();
 
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

export default FloorNode;