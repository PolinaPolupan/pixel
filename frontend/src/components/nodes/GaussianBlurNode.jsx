import {
    useReactFlow,
  } from '@xyflow/react';
  
  import LabeledHandle from '../handles/LabeledHandle';
  import NodeHeader from '../NodeHeader';
  import InputHandle from '../handles/InputHandle';
   
  function GaussianBlurNode({ id, data }) {
    const { updateNodeData } = useReactFlow();
   
    return (
      <div style={{ 
        minWidth: '100px'
      }}>
        <NodeHeader title={"Gaussian blur"}/>
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
          handleId="sizeX"
          handleLabel="SizeX"
          parameterType="INT"
        />
        <InputHandle 
          id={id}
          data={data}
          handleId="sizeY"
          handleLabel="SizeY"
          parameterType="INT"
        />
        <InputHandle 
          id={id}
          data={data}
          handleId="sigmaX"
          handleLabel="SigmaX"
          parameterType="DOUBLE"
        />
        <InputHandle 
          id={id}
          data={data}
          handleId="sigmaY"
          handleLabel="SigmaY"
          parameterType="DOUBLE"
        />
        <LabeledHandle 
          label="Files"
          type="source"
          position="right" 
          id="files"
          parameterType="FILENAMES_ARRAY"
        />
      </div>
    );
  }
  
  export default GaussianBlurNode;