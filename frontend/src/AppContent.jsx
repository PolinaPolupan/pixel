import React, { useCallback, useState, useRef, useEffect } from 'react';
import {
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
import { useNotification } from './utils/useNotification';
import { useGraphTransformation } from './utils/useGraphTransformation';
import { useScene } from './components/SceneContext';
import { useNodesApi } from './utils/useNodesApi';
import { GraphEditor } from "./components/GraphEditor.jsx";
import { GraphControls } from "./components/GraphControls.jsx";
import { useGraphExecution } from "./hooks/useGraphExecution.js";

function AppContent() {
    const { sceneId } = useScene();
    const [nodes, setNodes, onNodesChange] = useNodesState([]);
    const [edges, setEdges, onEdgesChange] = useEdgesState([]);
    const reactFlowWrapper = useRef(null);
    const { error, success, setError, setSuccess, clearError, clearSuccess } = useNotification();
    const transformGraphData = useGraphTransformation();
    const { screenToFlowPosition, getNodes, addNodes, fitView } = useReactFlow();
    const [contextMenu, setContextMenu] = useState(null);

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

    // Use our custom execution hook
    const { isProcessing, executeGraph } = useGraphExecution({
        sceneId,
        transformGraphData,
        setError,
        setSuccess
    });

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
            <GraphEditor
                nodes={nodes}
                edges={edges}
                nodeTypes={nodeTypes}
                onNodesChange={onNodesChange}
                onEdgesChange={onEdgesChange}
                onConnect={onConnect}
                isValidConnection={isValidConnection}
                colorMode="dark"
            />

            {/* Scene Info Panel */}
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

            {/* Debug Panel */}
            <Panel position="right-center" style={{ margin: '16px' }}>
                <DebugPanel />
            </Panel>

            {/* Controls Panel */}
            <GraphControls
                handlePlay={executeGraph}
                isProcessing={isProcessing}
                configLoading={configLoading}
                setSuccess={setSuccess}
                setError={setError}
            />

            {/* Notifications */}
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