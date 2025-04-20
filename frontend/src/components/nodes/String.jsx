import {
  useReactFlow,
} from '@xyflow/react';

import LabeledHandle from '../handles/LabeledHandle';
import NodeHeader from '../NodeHeader';
import InputHandle from '../handles/InputHandle';
 
export default function String({ id, data }) {
  const { updateNodeData } = useReactFlow();
 
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