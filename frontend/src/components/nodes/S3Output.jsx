import {
    useReactFlow,
  } from '@xyflow/react';
  
  import LabeledHandle from '../handles/LabeledHandle';
  import NodeHeader from '../NodeHeader';
  import InputHandle from '../handles/InputHandle';
   
  export default function S3Output({ id, data }) {
    const { updateNodeData } = useReactFlow();
   
    return (
      <div>
        <NodeHeader title={"S3 Output"}/>
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
            handleId="access_key_id"
            handleLabel="Access key"
            type = "text"
            parameterType="STRING"
        /> 
        <InputHandle 
            id={id}
            data={data}
            handleId="secret_access_key"
            handleLabel="Secret key"
            type = "text"
            parameterType="STRING"
        /> 
        <InputHandle 
            id={id}
            data={data}
            handleId="region"
            handleLabel="Region"
            type = "text"
            parameterType="STRING"
        /> 
        <InputHandle 
            id={id}
            data={data}
            handleId="bucket"
            handleLabel="Bucket"
            type = "text"
            parameterType="STRING"
        /> 
      </div>
    );
  }