import React, { useCallback, useState } from 'react';
import {
  ReactFlow,
  Background,
  useNodesState,
  useEdgesState,
  addEdge
} from '@xyflow/react';
 
import '@xyflow/react/dist/style.css';
import FloorNode from './components/nodes/FloorNode';
import InputNode from './components/nodes/InputNode';
import CombineNode from './components/nodes/CombineNode';
import OutputNode from './components/nodes/OutputNode';
import GaussianBlurNode from './components/nodes/GaussianBlurNode';
import S3InputNode from './components/nodes/S3InputNode';
import DebugPanel from './components/Debug';
import { getHandleParameterType } from './utils/parameterTypes';

const nodeTypes = {
  FloorNode, 
  InputNode,
  CombineNode,
  OutputNode,
  GaussianBlurNode,
  S3InputNode
};

const initialNodes = [
  {
  id: '1',
    type: 'FloorNode',
    position: { x: 165.19, y: 253.32 },
    data: { number: 56 }, 
  },
  {
    id: '2',
    type: 'InputNode',
    position: { x: -53.93, y: 210.68 },
    data: { files: ['Picture1.png', 'Picture3.png'] },
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

     }, 
  },
  {
    id: '4',
    type: 'OutputNode',
    position: { x: 100, y: 0 },
    data: { files: null, prefix: 'output1' }, // Placeholder for connected input
  },
  {
    id: '5',
    type: 'GaussianBlurNode',
    position: { x: 200, y: 0 },
    data: { files: null, sigmaX: 0, sigmaY: 0, sizeX: 1, sizeY: 1 }, // Placeholder for connected input
  },
  {
    id: '6',
    type: 'S3InputNode',
    position: { x: 300, y: 0 },
    data: { files: null }
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

  const [colorMode, setColorMode] = useState('dark');

  const onChange = (evt) => {
    setColorMode(evt.target.value);
  };

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
        colorMode={colorMode}
        fitView
      >
        <Background variant="dots" gap={12} size={1} />
        <DebugPanel />
      </ReactFlow>
    </div>
  );
}