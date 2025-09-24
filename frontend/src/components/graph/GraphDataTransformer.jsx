import React from 'react';
import { useGraphTransformation } from '../../hooks/useGraphTransformation.js';

const GraphDataTransformer = () => {
  const transformGraphData = useGraphTransformation();
  const transformedData = transformGraphData();

  return (
      <div>
        <h3>Transformed Graph Data</h3>
        <pre>{JSON.stringify(transformedData, null, 2)}</pre>
      </div>
  );
};

export default GraphDataTransformer;