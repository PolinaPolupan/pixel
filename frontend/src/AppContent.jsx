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
import { useNotification } from './utils/useNotification';
import { useGraphTransformation } from './utils/useGraphTransformation';
import { useScene } from './components/SceneContext';
import { useNodesApi } from './utils/useNodesApi';

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

    // Use the unified API hook
    const {
        nodeTypes,
        getHandleParameterType,
        canCastType,
        getDefaultData,
        isLoading: configLoading,
        error: configError,
        isReady
    } = useNodesApi();

    // Set error if there's a problem loading node configurations
    useEffect(() => {
        if (configError) {
            setError(`Failed to load node configurations: ${configError}`);
        }
    }, [configError, setError]);

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
    }, [nodes, getHandleParameterType, canCastType]);

    const onConnect = useCallback((params) => setEdges((els) => addEdge(params, els)), []);

    const handlePlay = async () => {
        setError(null);
        setIsProcessing(true);

        try {
            const graphData = transformGraphData();

            const response = await fetch(`http://localhost:8080/v1/scene/${sceneId}/graph`, {
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
        // Check if node config is available
        if (!isReady) {
            setError(`Cannot create node: ${configLoading ? 'Loading node configurations...' : 'Node configurations unavailable'}`);
            return;
        }

        const nodeIds = getNodes().map(node => parseInt(node.id));
        const newId = (Math.max(...nodeIds, 0) + 1).toString();

        const newNode = {
            id: newId,
            type,
            position,
            // Use the utility function from the API
            data: getDefaultData(type)
        };

        addNodes(newNode);
    }, [getNodes, addNodes, getDefaultData, isReady, configLoading, setError]);

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
                            backgroundColor: configLoading ? '#ffc107' : '#4caf50'
                        }} />
                        <span>
                            {configLoading ? 'Loading node configurations...' :
                                `Scene: ${sceneId ? String(sceneId).substring(0, 8) + '...' : 'Loading...'}`}
                        </span>
                    </div>
                </Panel>
                <Panel position="right-center" style={{ margin: '16px' }}>
                    <DebugPanel />
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
                            <PlayButton
                                onClick={handlePlay}
                                isProcessing={isProcessing}
                                disabled={configLoading}
                            />
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