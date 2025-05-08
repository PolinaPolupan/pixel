import React, { useMemo } from 'react';
import { useNodesConfig } from './NodesConfig';

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
        'FILENAMES_ARRAY': [],
        'STRING_ARRAY': [],
        'VECTOR2D': []
    }), []);

    // Extract node types (component mapping)
    const nodeTypes = useMemo(() => {
        if (!nodesConfig || Object.keys(nodesConfig).length === 0) {
            return {};
        }
        return Object.fromEntries(
            Object.entries(nodesConfig).map(([type, config]) => [type, config.component])
        );
    }, [nodesConfig]);

    // Extract node display details
    const nodeTypeDetails = useMemo(() => {
        if (!nodesConfig || Object.keys(nodesConfig).length === 0) {
            return {};
        }
        return Object.fromEntries(
            Object.entries(nodesConfig).map(([type, config]) => [type, config.display])
        );
    }, [nodesConfig]);

    // Handle parameter type utility
    const getHandleParameterType = useMemo(() => {
        return (nodeType, handleId, handleType) => {
            return nodesConfig[nodeType]?.handles?.[handleId]?.[handleType] || null;
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
    const getDefaultData = useMemo(() => {
        return (nodeType) => {
            return nodesConfig[nodeType]?.defaultData || {};
        };
    }, [nodesConfig]);

    // Return a comprehensive API
    return {
        // Core data
        nodesConfig,

        // Status
        isLoading,
        error,

        // Previously split functionality now combined
        nodeTypes,
        nodeTypeDetails,
        getHandleParameterType,
        canCastType,
        getDefaultData,

        // Utility to check if configs are ready
        isReady: !isLoading && !error && !!nodesConfig
    };
}

/**
 * For backward compatibility - these hooks use the unified API internally
 */
export function useHandleTypes() {
    const { getHandleParameterType, canCastType, isLoading, error } = useNodesApi();
    return { getHandleParameterType, canCastType, isLoading, error };
}

export function useNodeTypesConfig() {
    const { nodeTypes, nodeTypeDetails, isLoading, error } = useNodesApi();
    return { nodeTypes, nodeTypeDetails, isLoading, error };
}