import React, { useCallback, useState, useRef } from 'react';
import { useNodesState, useEdgesState, addEdge, Panel, useReactFlow } from '@xyflow/react';
import '@xyflow/react/dist/style.css';

import DebugPanel from '../ui/Debug.jsx';
import { NotificationPanel, NotificationKeyframes } from '../ui/NotificationPanel.jsx';
import ContextMenu from '../ui/ContextMenu.jsx';
import { useNodesApi } from '../../hooks/useNodesApi.js';
import { GraphEditor } from "../graph/GraphEditor.jsx";
import { GraphControls } from "../graph/GraphControls.jsx";
import { useGraphExecution } from "../../hooks/useGraphExecution.js";
import { useNotification } from "../../services/contexts/NotificationContext.jsx";
import {useGraphTransformation} from "../../hooks/useGraphTransformation.js";
import {useGraph} from "../../services/contexts/GraphContext.jsx";
import {useCreateNode} from "../../hooks/useCreateNode.js";

function AppContent() {
    const [nodes, setNodes, onNodesChange] = useNodesState([]);
    const [edges, setEdges, onEdgesChange] = useEdgesState([]);
    const reactFlowWrapper = useRef(null);
    const { error, success, clearError, clearSuccess } = useNotification();
    const { screenToFlowPosition, getNodes, addNodes } = useReactFlow();
    const [contextMenu, setContextMenu] = useState(null);
    const { createNode } = useCreateNode();
    const {  nodeReactComponents, getHandleType, canCastType, isLoading } = useNodesApi();
    const { graphId } = useGraph();
    const buildGraphBody = useGraphTransformation(graphId);
    const { isProcessing, executeGraph } = useGraphExecution(buildGraphBody);

    // Check if connection is valid
    const isValidConnection = useCallback((connection) => {
        if (connection.source === connection.target) return false;

        const sourceNode = nodes.find(n => n.id === connection.source);
        const targetNode = nodes.find(n => n.id === connection.target);

        const sourceType = getHandleType(sourceNode?.type, connection.sourceHandle, 'source');
        const targetType = getHandleType(targetNode?.type, connection.targetHandle, 'target');

        console.log(`Attempting connection: ${sourceNode} -> ${targetType}`);

        return sourceType && targetType && canCastType(sourceType, targetType);
    }, [nodes, getHandleType, canCastType]);

    // Connect nodes
    const onConnect = useCallback((params) => setEdges(els => addEdge(params, els)), []);

    // Handle right-click context menu
    const onContextMenu = useCallback((event) => {
        event.preventDefault();
        const rect = reactFlowWrapper.current.getBoundingClientRect();
        const x = event.clientX - rect.left;
        const y = event.clientY - rect.top;

        setContextMenu({
            position: { x, y },
            flowPosition: screenToFlowPosition({ x, y })
        });
    }, [screenToFlowPosition]);

    return (
        <div
            ref={reactFlowWrapper}
            onContextMenu={onContextMenu}
            style={{ width: '100%', height: '100%', position: 'relative' }}
        >
            <GraphEditor
                nodes={nodes}
                edges={edges}
                nodeTypes={nodeReactComponents}
                onNodesChange={onNodesChange}
                onEdgesChange={onEdgesChange}
                onConnect={onConnect}
                isValidConnection={isValidConnection}
                colorMode="dark"
            />

            {/* Controls */}
            <GraphControls
                handlePlay={executeGraph}
                isProcessing={isProcessing}
                configLoading={isLoading}
            />

            <Panel position="top-center">
                <span>
                    {isLoading ? 'Loading...' : `Graph: ${graphId}`}
                </span>
            </Panel>

            {/* Debug Panel */}
            <Panel position="right-center" style={{ margin: '16px' }}>
                <DebugPanel />
            </Panel>

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

            {/* Context Menu */}
            {contextMenu && (
                <ContextMenu
                    position={contextMenu.position}
                    onClose={() => setContextMenu(null)}
                    createNode={(type) => createNode(type, contextMenu.flowPosition)}
                />
            )}

            <NotificationKeyframes />
        </div>
    );
}

export default AppContent;