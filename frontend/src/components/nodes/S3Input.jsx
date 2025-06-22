import {
    useReactFlow,
  } from '@xyflow/react';
  
  import LabeledHandle from '../handles/LabeledHandle';
  import NodeHeader from '../NodeHeader';
  import InputHandle from '../handles/InputHandle';
   
  export default function S3Input({ id, data }) {
    const { updateNodeData } = useReactFlow();
   
    return (
      <div>
        <NodeHeader title={"S3 Input"}/>
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