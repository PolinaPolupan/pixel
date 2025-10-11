import {useCallback} from 'react';
import {useReactFlow} from '@xyflow/react';

export function useGraphTransformation() {
  const { getNodes, getEdges } = useReactFlow();

  return useCallback(() => {
    const nodes = getNodes();
    const edges = getEdges();

    // Helper: get only input fields from node data (exclude config fields)
    const getNodeInputs = (node) => {
      const data = node.data || {};
      // Exclude known config/meta keys
      const {config, ...inputs} = data;
      return inputs;
    };

    // Map for quick lookup of edges by target node and handle
    const edgesByTarget = {};
    edges.forEach(edge => {
      if (!edgesByTarget[edge.target]) edgesByTarget[edge.target] = {};

      edgesByTarget[edge.target][edge.targetHandle] = {
        source: edge.source,
        sourceHandle: edge.sourceHandle
      };
    });

    const transformedNodes = nodes.map(node => {
      const inputs = getNodeInputs(node);

      // Replace each input field if there's an incoming edge
      Object.keys(inputs).forEach(inputKey => {
        const edgeInfo = edgesByTarget[node.id]?.[inputKey];
        if (edgeInfo) {
          inputs[inputKey] = `@node:${parseInt(edgeInfo.source, 10)}:${edgeInfo.sourceHandle}`;
        }
      });

      return {
        id: parseInt(node.id, 10),
        type: node.type,
        inputs,
      };
    });

    return {nodes: transformedNodes};
  }, [getNodes, getEdges]);
}