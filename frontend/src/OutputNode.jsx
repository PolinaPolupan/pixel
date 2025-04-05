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
            label={`Files`}
            type="target"
            position="left" 
            id={`out_files`}
        />
        <InputHandle 
            id={id}
            data={data}
            title="Output"
            handleId="prefix"
            handleLabel="Prefix"
            type = "text"
        /> 
      </div>
    );
  }
  
  export default OutputNode;