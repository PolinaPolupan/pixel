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

function AppContent() {
    const [nodes, setNodes, onNodesChange] = useNodesState([]);
    const [edges, setEdges, onEdgesChange] = useEdgesState([]);
    const reactFlowWrapper = useRef(null);
    const { error, success, clearError, clearSuccess } = useNotification();
    const { screenToFlowPosition, getNodes, addNodes } = useReactFlow();
    const [contextMenu, setContextMenu] = useState(null);

    // Get node configurations
    const { nodesConfig, nodeReactComponents, getHandleType, canCastType, getDefaultInputs, isLoading } = useNodesApi();

    // Graph execution
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

    // Create new node
    const createNode = useCallback((type, position) => {
        const nodeIds = getNodes().map(n => parseInt(n.id));
        const newId = (Math.max(...nodeIds, 0) + 1).toString();

        addNodes({
            id: newId,
            type,
            position,
            data: {
                ...getDefaultInputs(type),
                config: nodesConfig[type]
            }
        });
    }, [getNodes, addNodes, getDefaultInputs, nodesConfig]);

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