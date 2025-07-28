import { useCallback } from 'react';
import { useReactFlow } from '@xyflow/react';

/**
 * Transforms React Flow graph data into server graph format.
 * - Converts node ids to integers
 * - Resolves input handles based on edges
 * - Strips out config/meta fields from inputs
 */
export function useGraphTransformation() {
  const { getNodes, getEdges } = useReactFlow();

  const transformGraphData = useCallback(() => {
    const nodes = getNodes();
    const edges = getEdges();

    // Helper: get only input fields from node data (exclude config fields)
    const getNodeInputs = (node) => {
      const data = node.data || {};
      // Exclude known config/meta keys
      const { config, ...inputs } = data;
      return inputs;
    };

    // Map for quick lookup of edges by target node and handle
    const edgesByTarget = {};
    edges.forEach(edge => {
      if (!edgesByTarget[edge.target]) edgesByTarget[edge.target] = {};
      // If handles are missing, default to 'output' for source, and first input for target
      const targetHandle = edge.targetHandle || Object.keys(getNodeInputs(nodes.find(n => n.id === edge.target)))[0];
      const sourceHandle = edge.sourceHandle || 'output';
      edgesByTarget[edge.target][targetHandle] = { source: edge.source, sourceHandle };
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

    return { nodes: transformedNodes };
  }, [getNodes, getEdges]);

  return transformGraphData;
}