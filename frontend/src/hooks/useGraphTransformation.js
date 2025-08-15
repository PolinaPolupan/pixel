import { useCallback } from 'react';
import { useReactFlow } from '@xyflow/react';

/**
 * Transforms React Flow graph data into server graph format.
 * - Converts node ids to integers
 * - Resolves input handles based on edges
 * - Strips out config/meta fields from inputs
 * - Handles prefixed source/target IDs
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

    // Helper: extract actual handle ID without prefix
    const cleanHandleId = (handleId) => {
      if (!handleId) return null;
      return handleId.replace(/^(source-|target-)/, '');
    };

    // Map for quick lookup of edges by target node and handle
    const edgesByTarget = {};
    edges.forEach(edge => {
      if (!edgesByTarget[edge.target]) edgesByTarget[edge.target] = {};

      // Clean the handle IDs by removing prefixes
      const cleanTargetHandle = cleanHandleId(edge.targetHandle) ||
          Object.keys(getNodeInputs(nodes.find(n => n.id === edge.target)))[0];

      const cleanSourceHandle = cleanHandleId(edge.sourceHandle) || 'output';

      edgesByTarget[edge.target][cleanTargetHandle] = {
        source: edge.source,
        sourceHandle: cleanSourceHandle
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

    return { nodes: transformedNodes };
  }, [getNodes, getEdges]);

  return transformGraphData;
}