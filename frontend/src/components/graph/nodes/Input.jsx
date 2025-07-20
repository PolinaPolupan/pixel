import {
  useReactFlow,
} from '@xyflow/react';

import LabeledHandle from '../../handles/LabeledHandle.jsx';
import FileUpload from '../../file/FileUpload.jsx'; // Updated import to FileUpload
import NodeHeader from '../../ui/NodeHeader.jsx';

export default function Input({ id }) {
  const { updateNodeData } = useReactFlow();

  const handleImagesSelected = (filePaths) => {
    updateNodeData(id, { files: filePaths }); // Update with relative paths (e.g., output/Picture.jpeg)
  };

  return (
    <div>
      <NodeHeader title="Input" />
      <FileUpload
        onFilesSelected={handleImagesSelected}
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