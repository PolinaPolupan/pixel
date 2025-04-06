import {
    useReactFlow,
  } from '@xyflow/react';
  
  import LabeledHandle from './LabeledHandle';
  import NodeHeader from './NodeHeader';
  import InputHandle from './InputHandle';
   
  function OutputNode({ id, data }) {
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
            title="Output"
            handleId="prefix"
            handleLabel="Prefix"
            type = "text"
            parameterType="STRING"
        /> 
      </div>
    );
  }
  
  export default OutputNode;