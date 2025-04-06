import React, { useCallback } from 'react';
import {
  ReactFlow,
  MiniMap,
  Controls,
  Background,
  useNodesState,
  useEdgesState,
  addEdge
} from '@xyflow/react';
 
import '@xyflow/react/dist/style.css';
import FloorNode from './FloorNode';
import InputNode from './InputNode';
import CombineNode from './CombineNode';
import OutputNode from './OutputNode';
import DebugPanel from './Debug';
import GraphDataTransformer from './GraphDataTransformer';
import { getHandleParameterType } from './parameterTypes';

const nodeTypes = {
  FloorNode, 
  InputNode,
  CombineNode,
  OutputNode
};
 
 
const initialNodes = [
  {
  id: '1',
    type: 'FloorNode',
    position: { x: 165.19, y: 253.32 },
    data: { number: 56 }, // Example input
  },
  {
    id: '2',
    type: 'InputNode',
    position: { x: -53.93, y: 210.68 },
    data: { files: ['Picture1.png', 'Picture3.png'] }, // Example input
  },
  {
    id: '3',
    type: 'CombineNode',
    position: { x: 122.73, y: 117.14 },
    data: { 
      files_0: null,
      files_1: null,
      files_2: null,
      files_3: null,
      files_4: null,
      files_5: null

     }, // Placeholder for connected input
  },
  {
    id: '4',
    type: 'OutputNode',
    position: { x: 100, y: 0 },
    data: { files: null, prefix: 'output1' }, // Placeholder for connected input
  },
];

const initialEdges = [];
 
export default function App() {
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

  const isValidConnection = (connection) => {
    const sourceNode = nodes.find(node => node.id === connection.source);
    const targetNode = nodes.find(node => node.id === connection.target);

    // Get parameter types based on node type and handle ID
    const sourceType = getHandleParameterType(sourceNode?.type, connection.sourceHandle, 'source');
    const targetType = getHandleParameterType(targetNode?.type, connection.targetHandle, 'target');

    // Allow connection if types match (or handle edge cases like null)
    if (!sourceType || !targetType) {
      console.warn('Unknown handle type:', { sourceType, targetType });
      return false;
    }

    return sourceType === targetType;
  };

  const onConnect = useCallback(
    (params) => setEdges((els) => addEdge(params, els)),
    [],
  );

 
  return (
    <div style={{ width: '100vw', height: '100vh' }}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        nodeTypes={nodeTypes}
        onNodesChange={onNodesChange}
        onConnect={onConnect}
        isValidConnection={isValidConnection}
        onEdgesChange={onEdgesChange}
        fitView
      >
        <Background variant="dots" gap={12} size={1} />
        <DebugPanel />
        <GraphDataTransformer />
      </ReactFlow>
    </div>
  );
}