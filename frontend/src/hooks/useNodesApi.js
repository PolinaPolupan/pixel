import { useMemo } from 'react';
import { useNodesConfig } from '../services/NodesConfig.jsx';
import Node from "../components/graph/Node.jsx";

/**
 * Unified hook for accessing node configurations and related utilities
 * Consolidates functionality from multiple hooks into a single API
 */
export function useNodesApi() {
    // Get base node configurations
    const { nodesConfig, isLoading, error } = useNodesConfig();

    // Type casting rules defined once
    const typeCastingRules = useMemo(() => ({
        'INT': ['FLOAT', 'DOUBLE'],
        'FLOAT': ['DOUBLE', 'INT'],
        'DOUBLE': ['FLOAT', 'INT'],
        'STRING': [],
        'FILEPATH_ARRAY': [],
        'STRING_ARRAY': [],
        'VECTOR2D': []
    }), []);

    // Extract node types (component mapping)
    const nodeReactComponents = useMemo(() => {
        if (!nodesConfig) return {};
        return Object.fromEntries(
            Object.keys(nodesConfig).map((nodeType) => [nodeType, Node])
        );
    }, [nodesConfig]);

    // Extract node display details
    const nodeDisplayInfo = useMemo(() => {
        if (!nodesConfig) return {};
        return Object.fromEntries(
            Object.entries(nodesConfig).map(([nodeType, config]) => [
                nodeType,
                config.display
            ])
        );
    }, [nodesConfig]);

    // Group nodes by category
    const nodesGroupedByCategory = useMemo(() => {
        if (!nodeDisplayInfo) return {};
        return Object.entries(nodeDisplayInfo).reduce((grouped, [nodeType, display]) => {
            const category = display.category || 'Other';
            if (!grouped[category]) grouped[category] = [];
            grouped[category].push({
                type: nodeType,
                display
            });
            return grouped;
        }, {});
    }, [nodeDisplayInfo]);

    const getHandleType = useMemo(() => {
        return (nodeType, handleId, handleType) => {
            if (handleType === 'source') {
                return nodesConfig[nodeType]?.inputHandles?.[handleId]?.source || null;
            } else {
                return nodesConfig[nodeType]?.outputHandles?.[handleId]?.target || null;
            }
        };
    }, [nodesConfig]);

    // Type casting utility
    const canCastType = useMemo(() => {
        return (sourceType, targetType) => {
            if (sourceType === targetType) return true;

            const allowedTargets = typeCastingRules[sourceType] || [];
            const canCast = allowedTargets.includes(targetType);

            if (!canCast && sourceType && targetType) {
                console.log(`Cannot cast ${sourceType} to ${targetType}`);
            }

            return canCast;
        };
    }, [typeCastingRules]);

    // Get default data for a node type
    const getDefaultInputs = useMemo(() => {
        return (nodeType) => {
            return Object.fromEntries(
                Object.entries(nodesConfig?.[nodeType]?.inputs || {}).map(
                    ([key, val]) => [key, val.default]
                )
            );
        };
    }, [nodesConfig]);

    return {
        // raw API
        nodesConfig,

        // status
        isLoading,
        error,
        isReady: !isLoading && !error && !!nodesConfig,

        // per-node helpers
        nodeReactComponents, // mapping nodeType -> React component
        nodeDisplayInfo,     // mapping nodeType -> { name, category, ... }
        getHandleType,       // get input/output type for a handle
        getDefaultInputs,    // get default input values
        canCastType,

        // grouped for menus
        nodesGroupedByCategory
    };
}