import React, { useCallback, useState, useRef, useEffect } from 'react';
import {
    Panel,
    useReactFlow
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';

import DebugPanel from './components/Debug';
import { NotificationPanel, NotificationKeyframes } from './components/NotificationPanel';
import ContextMenu from './components/contexts/ContextMenu.jsx';
import { useNotification } from './hooks/useNotification.js';
import { useGraphTransformation } from './hooks/useGraphTransformation.js';
import { useScene } from './components/contexts/SceneContext.jsx';
import { useNodesApi } from './hooks/useNodesApi.js';
import { GraphEditor } from "./components/GraphEditor.jsx";
import { GraphControls } from "./components/GraphControls.jsx";
import { useGraphExecution } from "./hooks/useGraphExecution.js";
import { useGraphState } from "./hooks/useGraphState.js";

function AppContent() {
    const { sceneId } = useScene();
    const reactFlowWrapper = useRef(null);
    const { error, success, setError, setSuccess, clearError, clearSuccess } = useNotification();
    const transformGraphData = useGraphTransformation();
    const { screenToFlowPosition, fitView } = useReactFlow();
    const [contextMenu, setContextMenu] = useState(null);

    // Use the custom graph state hook
    const {
        nodes,
        edges,
        onNodesChange,
        onEdgesChange,
        onConnect,
        isValidConnection,
        createNode
    } = useGraphState();

    // Use the unified API hook
    const {
        nodeTypes,
        isLoading: configLoading,
        error: configError
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