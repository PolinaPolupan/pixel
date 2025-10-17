import {useCallback} from 'react';
import {useReactFlow} from '@xyflow/react';

export function useGraphTransformation() {
  const { getNodes, getEdges } = useReactFlow();

  return useCallback(() => {
    const nodes = getNodes();
    const edges = getEdges();

    const getNodeInputs = (node) => {
      const data = node.data || {};
      const {config, ...inputs} = data;
      return inputs;
    };

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