import React from 'react';
import { useReactFlow } from '@xyflow/react';

const GraphDataTransformer = () => {
  const { getNodes, getEdges } = useReactFlow();

  const transformGraphData = () => {
    const nodes = getNodes();
    const edges = getEdges();

    // Create a map of edges for quick lookup (source -> target mappings)
    const edgeMap = {};
    edges.forEach(edge => {
      if (!edgeMap[edge.source]) {
        edgeMap[edge.source] = [];
      }
      edgeMap[edge.source].push({
        target: edge.target,
        targetHandle: edge.targetHandle, // e.g., "files_0", "number"
        sourceHandle: edge.sourceHandle, // e.g., "files", "number"
      });
    });

    // Transform nodes into the desired format
    const transformedNodes = nodes.map(node => {
      const inputs = { ...node.data }; // Copy node.data as the base inputs

      // Replace connected inputs with references (e.g., "@node:ID:KEY")
      Object.keys(inputs).forEach(inputKey => {
        // Check if this input is connected via an edge
        const incomingEdges = edges.filter(
          edge => edge.target === node.id && edge.targetHandle === inputKey
        );
        if (incomingEdges.length > 0) {
          const sourceNodeId = incomingEdges[0].source;
          const sourceHandle = incomingEdges[0].sourceHandle || 'output'; // Default to 'output' if no handle
          inputs[inputKey] = `@node:${sourceNodeId}:${sourceHandle}`;
        }
      });

      return {
        id: parseInt(node.id, 10), // Convert ID to integer if needed
        type: node.type,
        inputs,
      };
    });

    return {
      nodes: transformedNodes,
    };
  };

  // Get the transformed data
  const transformedData = transformGraphData();

  return (
    <div>
      <h3>Transformed Graph Data</h3>
      <pre>{JSON.stringify(transformedData, null, 2)}</pre>
    </div>
  );
};

export default GraphDataTransformer;