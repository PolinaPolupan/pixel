import {
  useReactFlow,
} from '@xyflow/react';

import LabeledHandle from '../handles/LabeledHandle';
import FileUpload from '../FileUpload'; // Updated import to FileUpload
import NodeHeader from '../NodeHeader';
import { useNotification } from '../../hooks/useNotification.js';

export default function Input({ id, data }) {
  const { updateNodeData } = useReactFlow();

  const handleImagesSelected = (filePaths) => {
    updateNodeData(id, { files: filePaths }); // Update with relative paths (e.g., output/Picture.jpeg)
  };

  return (
    <div>
      <NodeHeader title="Input" />
      <FileUpload
        onFilesSelected={handleImagesSelected}
        setError={useNotification}
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