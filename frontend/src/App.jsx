import React, { useCallback } from 'react';
import {
  ReactFlow,
  MiniMap,
  Controls,
  Background,
  useNodesState,
  useEdgesState,
  addEdge,
} from '@xyflow/react';
 
import '@xyflow/react/dist/style.css';
import FloorNode from './FloorNode';
import InputNode from './InputNode';
import CombineNode from './CombineNode';
import OutputNode from './OutputNode';

const nodeTypes = {
  FloorNode, 
  InputNode,
  CombineNode,
  OutputNode
};
 
 
const initialNodes = [
  {
    type: 'FloorNode',
    id: '1',
    data: { label: 'Floor' },
    position: { x: 0, y: 0 },
  },
  {
    type: 'InputNode',
    id: '2',
    data: { label: 'Floor' },
    position: { x: 10, y: 0 },
  },
  {
    type: 'CombineNode',
    id: '3',
    data: { label: 'Floor' },
    position: { x: 20, y: 0 }
  },
  {
    type: 'OutputNode',
    id: '4',
    data: { label: 'Floor' },
    position: { x: 100, y: 0 },
  },
];

const initialEdges = [];
 
export default function App() {
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);
 
  const onConnect = (params) => {
    setEdges((eds) => [...eds, { ...params, id: `${params.source}-${params.target}` }]);
    
    // Update the target node's data to reflect it's connected
    setNodes((nds) =>
      nds.map((node) => {
        if (node.id === params.target) {
          return {
            ...node,
            data: { ...node.data, isConnected: true, text: node.data.text }, // Keep existing value or fetch from source
          };
        }
        return node;
      })
    );
  };

  const onNodesDelete = useCallback(
    (deleted) => {
      setEdges(
        deleted.reduce((acc, node) => {
          const incomers = getIncomers(node, nodes, edges);
          const outgoers = getOutgoers(node, nodes, edges);
          const connectedEdges = getConnectedEdges([node], edges);
 
          const remainingEdges = acc.filter(
            (edge) => !connectedEdges.includes(edge),
          );
 
          const createdEdges = incomers.flatMap(({ id: source }) =>
            outgoers.map(({ id: target }) => ({
              id: `${source}->${target}`,
              source,
              target,
            })),
          );
 
          return [...remainingEdges, ...createdEdges];
        }, edges),
      );
    },
    [nodes, edges],
  );
 
  return (
    <div style={{ width: '100vw', height: '100vh' }}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        nodeTypes={nodeTypes}
        onNodesChange={onNodesChange}
        onNodesDelete={onNodesDelete}
        onEdgesChange={onEdgesChange}
        onConnect={onConnect}
        fitView
      >
        <Background variant="dots" gap={12} size={1} />
      </ReactFlow>
    </div>
  );
}