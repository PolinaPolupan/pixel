import {
    useReactFlow,
  } from '@xyflow/react';
  
  import LabeledHandle from '../handles/LabeledHandle';
  import NodeHeader from '../NodeHeader';
  import InputHandle from '../handles/InputHandle';
   
  export default function Output({ id, data }) {
    const { updateNodeData } = useReactFlow();
   
    return (
      <div>
        <NodeHeader title={"Output"}/>
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
            handleId="prefix"
            handleLabel="Prefix"
            type = "text"
            parameterType="STRING"
        /> 
        <InputHandle 
            id={id}
            data={data}
            handleId="folder"
            handleLabel="Folder"
            type = "text"
            parameterType="STRING"
        /> 
      </div>
    );
  }