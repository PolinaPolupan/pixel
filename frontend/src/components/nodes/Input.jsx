import {
  useReactFlow,
} from '@xyflow/react';

import LabeledHandle from '../handles/LabeledHandle';
import ImageUpload from '../ImageUpload';
import NodeHeader from '../NodeHeader';
 
export default function Input({ id, data }) {
  const { updateNodeData } = useReactFlow();

  const handleImagesSelected = (fileUrls) => {
    updateNodeData(id, { files: fileUrls }); // Update with URLs
  };
 
  return (
    <div>
      <NodeHeader title={"Input"}/>
      <ImageUpload
        onImagesSelected={handleImagesSelected}
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