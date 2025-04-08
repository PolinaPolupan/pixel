import { useState } from 'react';
import LabeledHandle from '../handles/LabeledHandle';
import NodeHeader from '../NodeHeader';

export default function Combine({ data }) {
  const [inputCount, setInputCount] = useState(data?.inputs ? Object.keys(data.inputs).length : 1);
  
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
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
      }}>
        <NodeHeader title={"Combine"}/>
        <button 
          onClick={addHandle}
          style={{ 
            background: 'rgba(0, 0, 0, 0)', 
            color: 'rgb(255, 255, 255)', 
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '14px',
            marginRight: '5px',
            border: 'none'
          }}
        >+</button>
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