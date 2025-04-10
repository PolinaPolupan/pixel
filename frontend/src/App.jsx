import React, { useCallback, useState } from 'react';
import {
  ReactFlow,
  Background,
  useNodesState,
  useEdgesState,
  addEdge,
  Panel,
  ReactFlowProvider,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';

import DebugPanel from './components/Debug';
import { NotificationPanel, NotificationKeyframes } from './components/NotificationPanel';
import { PlayButton } from './components/PlayButton';
import Floor from './components/nodes/Floor';
import Input from './components/nodes/Input';
import Combine from './components/nodes/Combine';
import Output from './components/nodes/Output';
import GaussianBlur from './components/nodes/GaussianBlur';
import S3Input from './components/nodes/S3Input';
import S3Output from './components/nodes/S3Output';
import { getHandleParameterType, canCastType } from './utils/parameterTypes';

// Import custom hooks
import { useNotification } from './utils/useNotification';
import { useGraphTransformation } from './utils/useGraphTransformation';

// API configuration
const API_URL = 'http://localhost:8080/v1/graph';

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
  { id: '2', type: 'Input', position: { x: -53.93, y: 210.68 }, data: { files: [] } },
  { id: '3', type: 'Combine', position: { x: 122.73, y: 117.14 }, data: { files_0: [], files_1: [], files_2: [], files_3: [], files_4: [], files_5: [] } },
  { id: '4', type: 'Output', position: { x: 100, y: 0 }, data: { files: [], prefix: 'output1' } },
  { id: '5', type: 'GaussianBlur', position: { x: 200, y: 0 }, data: { files: [], sizeX: 1, sizeY: 1, sigmaX: 0, sigmaY: 0 } },
  { id: '6', type: 'S3Input', position: { x: 300, y: 0 }, data: { access_key_id: "", secret_access_key: "", region: "", bucket: "" } },
  { id: '7', type: 'S3Output', position: { x: 300, y: 0 }, data: { files: [], access_key_id: "", secret_access_key: "", region: "", bucket: "" } },
];

const initialEdges = [];

function AppContent() {
  // Flow states
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);
  const [isProcessing, setIsProcessing] = useState(false);
  const [colorMode, setColorMode] = useState('dark');
  
  // Custom hooks
  const { error, success, setError, setSuccess, clearError, clearSuccess } = useNotification();
  const transformGraphData = useGraphTransformation();

  // Connection validation
  const isValidConnection = useCallback((connection) => {
    const sourceNode = nodes.find(node => node.id === connection.source);
    const targetNode = nodes.find(node => node.id === connection.target);

    const sourceType = getHandleParameterType(sourceNode?.type, connection.sourceHandle, 'source');
    const targetType = getHandleParameterType(targetNode?.type, connection.targetHandle, 'target');

    if (!sourceType || !targetType) {
      return false;
    }

    return canCastType(sourceType, targetType);
  }, [nodes]);

  // Edge connection handler
  const onConnect = useCallback((params) => setEdges((els) => addEdge(params, els)), []);

  // Process graph handler
  const handlePlay = async () => {
    setIsProcessing(true);
    setError(null);

    try {
      const graphData = transformGraphData();
      const response = await fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(graphData),
      });

      if (!response.ok) {
        const errorText = await response.text();
        let errorMessage = `Server error (${response.status})`;
        try {
          const errorJson = JSON.parse(errorText);
          errorMessage = errorJson.ex || errorText;
        } catch (e) {
          // Fallback to default error message
        }
        throw new Error(errorMessage);
      }

      setSuccess('Graph processing started successfully!');
    } catch (error) {
      setError(error.message);
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
        
        {/* Play button panel */}
        <Panel position="bottom-center">
          <PlayButton onClick={handlePlay} isProcessing={isProcessing} />
        </Panel>
        
        {/* Notification panels */}
        {error && (
          <Panel position="top-center">
            <NotificationPanel type="error" message={error} onDismiss={clearError} />
          </Panel>
        )}
        
        {success && (
          <Panel position="top-center">
            <NotificationPanel type="success" message={success} onDismiss={clearSuccess} />
          </Panel>
        )}
        
        <NotificationKeyframes />
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