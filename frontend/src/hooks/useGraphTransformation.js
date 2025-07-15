import { useCallback } from 'react';
import { useReactFlow } from '@xyflow/react';

export function useGraphTransformation() {
  const { getNodes, getEdges } = useReactFlow();
  
  const transformGraphData = useCallback(() => {
    const nodes = getNodes();
    const edges = getEdges();

    const transformedNodes = nodes.map(node => {
      const inputs = { ...node.data };
      Object.keys(inputs).forEach(inputKey => {
        const incomingEdges = edges.filter(
          edge => edge.target === node.id && edge.targetHandle === inputKey
        );
        if (incomingEdges.length > 0) {
          const sourceNodeId = incomingEdges[0].source;
          const sourceHandle = incomingEdges[0].sourceHandle || 'output';
          inputs[inputKey] = `@node:${sourceNodeId}:${sourceHandle}`;
        }
      });

      return {
        id: parseInt(node.id, 10),
        type: node.type,
        inputs,
      };
    });

    return { nodes: transformedNodes };
  }, [getNodes, getEdges]);

  return transformGraphData;
}