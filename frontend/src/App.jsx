import React, { useCallback, useState } from 'react';
import {
  ReactFlow,
  Background,
  useNodesState,
  useEdgesState,
  addEdge,
  Panel,
  useReactFlow,
  ReactFlowProvider, // Add this
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import Floor from './components/nodes/Floor';
import Input from './components/nodes/Input';
import Combine from './components/nodes/Combine';
import Output from './components/nodes/Output';
import GaussianBlur from './components/nodes/GaussianBlur';
import S3Input from './components/nodes/S3Input';
import S3Output from './components/nodes/S3Output';
import DebugPanel from './components/Debug';
import { getHandleParameterType, canCastType } from './utils/parameterTypes';

const nodeTypes = {
  Floor,
  Input,
  Combine,
  Output,
  GaussianBlur,
  S3Input,
  S3Output,
};

const initialNodes = [
  { id: '1', type: 'Floor', position: { x: 165.19, y: 253.32 }, data: { number: 56 } },
  { id: '2', type: 'Input', position: { x: -53.93, y: 210.68 }, data: { files: ['Picture1.png', 'Picture3.png'] } },
  { id: '3', type: 'Combine', position: { x: 122.73, y: 117.14 }, data: { files_0: null, files_1: null, files_2: null, files_3: null, files_4: null, files_5: null } },
  { id: '4', type: 'Output', position: { x: 100, y: 0 }, data: { files: null, prefix: 'output1' } },
  { id: '5', type: 'GaussianBlur', position: { x: 200, y: 0 }, data: { files: null, sizeX: 1, sizeY: 1, sigmaX: 0, sigmaY: 0 } },
  { id: '6', type: 'S3Input', position: { x: 300, y: 0 }, data: { access_key_id: null, secret_access_key: null, region: null, bucket: null } },
  { id: '7', type: 'S3Output', position: { x: 300, y: 0 }, data: { files: null, access_key_id: null, secret_access_key: null, region: null, bucket: null } },
];

const initialEdges = [];

function AppContent() {
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);
  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState(null);
  const { getNodes, getEdges } = useReactFlow();

  const isValidConnection = (connection) => {
    const sourceNode = nodes.find(node => node.id === connection.source);
    const targetNode = nodes.find(node => node.id === connection.target);

    const sourceType = getHandleParameterType(sourceNode?.type, connection.sourceHandle, 'source');
    const targetType = getHandleParameterType(targetNode?.type, connection.targetHandle, 'target');

    if (!sourceType || !targetType) {
      console.warn('Unknown handle type:', { sourceType, targetType });
      return false;
    }

    return canCastType(sourceType, targetType);
  };

  const onConnect = useCallback((params) => setEdges((els) => addEdge(params, els)), []);

  const [colorMode, setColorMode] = useState('dark');

  const onChange = (evt) => {
    setColorMode(evt.target.value);
  };

  const transformGraphData = () => {
    const nodes = getNodes();
    const edges = getEdges();

    const edgeMap = {};
    edges.forEach(edge => {
      if (!edgeMap[edge.source]) {
        edgeMap[edge.source] = [];
      }
      edgeMap[edge.source].push({
        target: edge.target,
        targetHandle: edge.targetHandle,
        sourceHandle: edge.sourceHandle,
      });
    });

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
  };

  const handlePlay = async () => {
    console.log('Play button clicked');
    setIsProcessing(true);
    setError(null);

    try {
      const graphData = transformGraphData();
      console.log('Sending graph data to server:', graphData);

      const response = await fetch('http://localhost:8080/v1/graph', { // Use service name for Docker
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(graphData),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Server responded with ${response.status}: ${errorText}`);
      }

      const result = await response.json();
      console.log('Server response:', result);
      alert('Graph processing started successfully!');
    } catch (error) {
      console.error('Error sending graph data:', error);
      setError(error.message);
      alert(`Error: ${error.message}`);
    } finally {
      setIsProcessing(false);
    }
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
        <Panel position="bottom-center">
          <button
            onClick={handlePlay}
            disabled={isProcessing}
            style={{
              width: '40px',
              height: '40px',
              borderRadius: '8px',
              background: isProcessing ? 'rgb(100, 100, 100)' : 'rgb(0, 110, 0)',
              border: '1px solid rgba(255, 255, 255, 0.2)',
              color: 'rgb(194, 255, 212)',
              cursor: isProcessing ? 'wait' : 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '18px',
              padding: 0,
              lineHeight: 1,
              boxSizing: 'border-box',
              boxShadow: '0 2px 4px rgba(0, 0, 0, 0.3)',
              transition: 'background 0.2s, border-color 0.2s, transform 0.1s',
            }}
            onMouseOver={(e) => {
              if (!isProcessing) {
                e.target.style.background = 'rgb(0, 200, 0)';
                e.target.style.borderColor = 'rgba(255, 255, 255, 0.4)';
                e.target.style.transform = 'scale(1.05)';
              }
            }}
            onMouseOut={(e) => {
              if (!isProcessing) {
                e.target.style.background = 'rgb(0, 110, 0)';
                e.target.style.borderColor = 'rgba(255, 255, 255, 0.2)';
                e.target.style.transform = 'scale(1)';
              }
            }}
          >
            {isProcessing ? '⏳' : '▶'}
          </button>
        </Panel>
        {error && (
          <Panel position="top-center" style={{ color: 'red', background: 'rgba(0,0,0,0.7)', padding: '10px', borderRadius: '5px' }}>
            Error: {error}
          </Panel>
        )}
      </ReactFlow>
    </div>
  );
}

export default function App() {
  return (
    <ReactFlowProvider>
      <AppContent />
    </ReactFlowProvider>
  );
}