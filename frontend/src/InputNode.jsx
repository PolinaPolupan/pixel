import {
  useReactFlow,
} from '@xyflow/react';

import LabeledHandle from './LabeledHandle';
import ImageUpload from './ImageUpload';
import NodeHeader from './NodeHeader';
 
function InputNode({ id, data }) {
  const { updateNodeData } = useReactFlow();
 
  return (
    <div>
      <NodeHeader title={"Input"}/>
      <ImageUpload/>
      <LabeledHandle 
        label="Files"
        type="source"
        position="right" 
        id="files"
      />
    </div>
  );
}

export default InputNode;