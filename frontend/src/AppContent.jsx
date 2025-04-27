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
import ProgressBar from './components/ProgressBar';
import { getHandleParameterType, canCastType } from './utils/parameterTypes';
import { useNotification } from './utils/useNotification';
import { useGraphTransformation } from './utils/useGraphTransformation';
import { useScene } from './components/SceneContext';
import { nodeTypes } from './utils/nodeTypes';
import { nodesConfig } from './utils/NodesConfig';


function AppContent() {
    const { sceneId } = useScene();
    const [nodes, setNodes, onNodesChange] = useNodesState([]);
    const [edges, setEdges, onEdgesChange] = useEdgesState([]);
    const [colorMode, setColorMode] = useState('dark');
    const [contextMenu, setContextMenu] = useState(null);
    const reactFlowWrapper = useRef(null);
    const { error, success, setError, setSuccess, clearError, clearSuccess } = useNotification();
    const transformGraphData = useGraphTransformation();
    const { screenToFlowPosition, getNodes, addNodes, fitView } = useReactFlow();
    const [isProcessing, setIsProcessing] = useState(false);
    const [batchSize, setBatchSize] = useState(50); // Default batchSize
    const [batchSizeInput, setBatchSizeInput] = useState('50'); // Input value as string

    const handleBatchSizeChange = (e) => {
        const inputValue = e.target.value;
        setBatchSizeInput(inputValue);

        // Only update the actual batchSize if there's a valid number
        if (inputValue.trim() !== '') {
            const value = parseInt(inputValue, 10);
            if (!isNaN(value)) {
                setBatchSize(value);
            }
        }
    };

    // When input field loses focus, ensure a valid value
    const handleBatchSizeBlur = () => {
        if (batchSizeInput.trim() === '' || isNaN(parseInt(batchSizeInput, 10))) {
            setBatchSizeInput('50');
            setBatchSize(50);
        }
    };

    useEffect(() => {
        const resizeObserver = new ResizeObserver(() => {
            if (reactFlowWrapper.current) {
                fitView({ duration: 200 });
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

    const onConnect = useCallback((params) => setEdges((els) => addEdge(params, els)), []);

    const handlePlay = async () => {
        setError(null);
        setIsProcessing(true);

        try {
            const graphData = transformGraphData();

            const response = await fetch(`http://localhost:8080/v1/scene/${sceneId}/graph?batchSize=${batchSize}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(graphData),
                credentials: 'include'
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Server error (${response.status}): ${errorText}`);
            }
        } catch (error) {
            console.error('Error during graph processing:', error);
            setError(error.message);
            setIsProcessing(false);
        }
    };

    const createNode = useCallback((type, position) => {
        const nodeIds = getNodes().map(node => parseInt(node.id));
        const newId = (Math.max(...nodeIds, 0) + 1).toString();

        const newNode = {
            id: newId,
            type,
            position,
            data: nodesConfig[type].defaultData
        };

        addNodes(newNode);
    }, [getNodes, addNodes]);

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
                        <span>
            Scene: {sceneId ? String(sceneId).substring(0, 8) + '...' : 'Loading...'}
          </span>
                    </div>
                </Panel>

                <Panel position="bottom-center" style={{ margin: '16px' }}>
                    <div style={{
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        gap: '12px',
                        maxWidth: '400px',
                        minHeight: '74px',
                    }}>
                        <div style={{ height: '40px', display: 'flex', alignItems: 'center' }}>
                            <PlayButton onClick={handlePlay} isProcessing={isProcessing} />
                        </div>
                        <div style={{ height: '26px', width: '300px' }}>
                            <ProgressBar
                                sceneId={sceneId}
                                setIsProcessing={setIsProcessing}
                                setSuccess={setSuccess}
                            />
                        </div>
                    </div>
                </Panel>

                <Panel position="bottom-right">
                    <div style={{
                      padding: '8px 12px',
                      background: 'rgba(0, 0, 0, 0)',
                      borderRadius: '4px',
                      color: 'white',
                      fontSize: '12px',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px'
                    }}>
                      <label htmlFor="batchSize">Batch Size:</label>
                      <div style={{ position: 'relative', display: 'inline-block' }}>
                        <input
                          id="batchSize"
                          type="text"
                          className="text-field"
                          value={batchSizeInput}
                          onChange={handleBatchSizeChange}
                          onBlur={handleBatchSizeBlur}
                          style={{
                            width: `100px`,
                            textAlign: 'left',
                            minWidth: '100px'
                          }}
                        />
                      </div>
                    </div>
                  </Panel>
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