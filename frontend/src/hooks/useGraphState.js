import { useCallback } from 'react';
import {
    useNodesState,
    useEdgesState,
    addEdge,
    useReactFlow
} from '@xyflow/react';
import { useNotification } from './useNotification.js';
import { useNodesApi } from './useNodesApi.js';

/**
 * Custom hook to manage graph state including nodes, edges, and connections
 * Extracted from AppContent.jsx to separate concerns
 */
export function useGraphState() {
    const [nodes, , onNodesChange] = useNodesState([]);
    const [edges, setEdges, onEdgesChange] = useEdgesState([]);
    const { setError } = useNotification();
    const { getNodes, addNodes } = useReactFlow();
    
    const {
        getHandleParameterType,
        canCastType,
        getDefaultData,
        isLoading: configLoading,
        isReady
    } = useNodesApi();

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

    const onConnect = useCallback((params) => setEdges((els) => addEdge(params, els)), [setEdges]);

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

    return {
        nodes,
        edges,
        onNodesChange,
        onEdgesChange,
        onConnect,
        isValidConnection,
        createNode
    };
}