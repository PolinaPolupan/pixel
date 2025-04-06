import { useState } from 'react';
import { Handle, Position } from '@xyflow/react';
import LabeledHandle from './LabeledHandle';
import NodeHeader from './NodeHeader';

function CombineNode({ data }) {
  // Start with 1 input (files_0)
  const [inputCount, setInputCount] = useState(data?.inputs ? Object.keys(data.inputs).length : 1);
  
  // Simple function to add one more handle
  const addHandle = () => {
    if (inputCount < 10) {
      setInputCount(inputCount + 1);
    }
  };

  return (
    <div style={{ 
      minWidth: '100px'
    }}>
      <div style={{ 
        fontWeight: 'bold', 
        marginBottom: '10px',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
      }}>
        <NodeHeader title={"Combine"}/>
        <button 
          onClick={addHandle}
          style={{ 
            width: '20px', 
            height: '20px', 
            borderRadius: '50%', 
            border: '1px solid #ccc',
            background: '#f5f5f5',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '14px',
            padding: 0,
            margin: '5px'
          }}
        >
          +
        </button>
      </div>
      
        {Array.from({ length: inputCount }).map((_, index) => (
            <div key={index}>
            <LabeledHandle 
                label={`Files ${index}`}
                type="target"
                position="left" 
                id={`files_${index}`}
                parameterType="FILENAMES_ARRAY"
            />
            </div>
        ))}
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

export default CombineNode;