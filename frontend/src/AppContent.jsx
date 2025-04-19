import React, { useCallback, useState, useRef, useEffect } from 'react';
import {
  ReactFlow,
  Background,
  useNodesState,
  useEdgesState,
  addEdge,
  Panel,
  useReactFlow
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';

import DebugPanel from './components/Debug';
import { NotificationPanel, NotificationKeyframes } from './components/NotificationPanel';
import ContextMenu from './components/ContextMenu';
import { PlayButton } from './components/PlayButton';
import { getHandleParameterType, canCastType } from './utils/parameterTypes';
import { useNotification } from './components/NotificationContext';
import { useGraphTransformation } from './utils/useGraphTransformation';
import { useScene } from './components/SceneContext';
import { nodeTypes } from './utils/nodeTypes';

function AppContent() {
  // Get scene context
  const { sceneId } = useScene();
  
  // Flow states
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [isProcessing, setIsProcessing] = useState(false);
  const [colorMode, setColorMode] = useState('dark');
  const [contextMenu, setContextMenu] = useState(null);
  const reactFlowWrapper = useRef(null);
  
  // Custom hooks
  const { error, success, setError, setSuccess, clearError, clearSuccess } = useNotification();
  const transformGraphData = useGraphTransformation();
  const { screenToFlowPosition, getNodes, addNodes, fitView } = useReactFlow();

  // Resize observer to update canvas on panel resize
  useEffect(() => {
    const resizeObserver = new ResizeObserver(() => {
      if (reactFlowWrapper.current) {
        fitView({ duration: 200 }); // Adjust viewport to new dimensions
      }
    });

    if (reactFlowWrapper.current) {
      resizeObserver.observe(reactFlowWrapper.current);
    }

    return () => {
      if (reactFlowWrapper.current) {
        resizeObserver.unobserve(reactFlowWrapper.current);
      }
    };
  }, [fitView]);

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
      
      // Use the scene ID from context in the API URL
      const response = await fetch(`http://localhost:8080/v1/scene/${sceneId}/graph`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(graphData),
        credentials: 'include'
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

  const createNode = useCallback((type, position) => {
    // Get highest node ID to ensure unique IDs
    const nodeIds = getNodes().map(node => parseInt(node.id));
    const newId = (Math.max(...nodeIds, 0) + 1).toString();
    
    // Default data for each node type
    const defaultData = {
      Input: { files: [] },
      Output: { files: [], prefix: 'output' },
      GaussianBlur: { files: [], sizeX: 3, sizeY: 3, sigmaX: 1, sigmaY: 1 },
      Combine: { files_0: [], files_1: [], files_2: [], files_3: [], files_4: [], files_5: [] },
      Floor: { number: 0 },
      S3Input: { access_key_id: "", secret_access_key: "", region: "", bucket: "" },
      S3Output: { files: [], access_key_id: "", secret_access_key: "", region: "", bucket: "" }
    };
    
    // Create new node
    const newNode = {
      id: newId,
      type,
      position,
      data: defaultData[type] || {}
    };
    
    addNodes(newNode);
  }, [getNodes, addNodes]);

  // Handle right-click to open context menu
  const onContextMenu = useCallback(
    (event) => {
      event.preventDefault();
      
      const boundingRect = reactFlowWrapper.current.getBoundingClientRect();
      const position = {
        x: event.clientX - boundingRect.left,
        y: event.clientY - boundingRect.top,
      };
      
      setContextMenu({
        position,
        flowPosition: screenToFlowPosition({
          x: event.clientX - boundingRect.left,
          y: event.clientY - boundingRect.top,
        }),
      });
    },
    [screenToFlowPosition]
  );

  // Close context menu
  const closeContextMenu = useCallback(() => {
    setContextMenu(null);
  }, []);

  return (
    <div 
      style={{ width: '100%', height: '100%', position: 'relative', overflow: 'hidden' }}
      ref={reactFlowWrapper}
      onContextMenu={onContextMenu}
    >
      <ReactFlow
        nodes={nodes}
        edges={edges}
        nodeTypes={nodeTypes}
        onNodesChange={onNodesChange}
        onConnect={onConnect}
        isValidConnection={isValidConnection}
        onEdgesChange={onEdgesChange}
        colorMode={colorMode}
        style={{ width: '100%', height: '100%' }}
        fitView
      >
        <Background variant="dots" gap={12} size={1} />
        
        {/* Scene info panel */}
        <Panel position="top-center">
          <div style={{
            padding: '8px 12px',
            background: 'rgba(0, 0, 0, 0.5)',
            borderRadius: '4px',
            color: 'white',
            fontSize: '12px',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}>
            <div style={{
              width: '8px',
              height: '8px',
              borderRadius: '50%',
              backgroundColor: '#4caf50'
            }} />
            <span>Scene: {sceneId ? sceneId.substring(0, 8) + '...' : 'Loading...'}</span>
          </div>
        </Panel>
        {/* <DebugPanel /> */}
        
        {/* Play button panel */}
        <Panel position="bottom-center" style={{ margin: '16px' }}>
          <div style={{ maxWidth: '100px', maxHeight: '40px' }}>
            <PlayButton onClick={handlePlay} isProcessing={isProcessing} />
          </div>
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
      {/* Context Menu */}
      {contextMenu && (
        <ContextMenu
          position={contextMenu.position}
          onClose={closeContextMenu}
          createNode={(type) => createNode(type, contextMenu.flowPosition)}
        />
      )}
    </div>
  );
}

export default AppContent;