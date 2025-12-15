import { useMemo } from 'react';
import { useNodesConfig } from '../services/NodesConfig.jsx';
import Node from "../components/graph/Node.jsx";

export function useNodesApi() {
    const { nodesConfig, isLoading, error } = useNodesConfig();

    const typeCastingRules = useMemo(() => ({
        'INT': ['FLOAT', 'DOUBLE', 'DEFAULT'],
        'FLOAT':  ['DOUBLE', 'INT', 'DEFAULT'],
        'DOUBLE': ['FLOAT', 'INT', 'DEFAULT'],
        'STRING': ['DEFAULT'],
        'FILEPATH_ARRAY': ['DEFAULT'],
        'STRING_ARRAY': ['DEFAULT'],
        'VECTOR2D': ['DEFAULT'],
        'DEFAULT': ['INT', 'FLOAT', 'DOUBLE', 'STRING', 'FILEPATH_ARRAY', 'STRING_ARRAY', 'VECTOR2D']
    }), []);

    const nodeReactComponents = useMemo(() => {
        if (!nodesConfig) return {};
        return Object.fromEntries(
            Object.keys(nodesConfig).map((nodeType) => [nodeType, Node])
        );
    }, [nodesConfig]);

    const nodeDisplayInfo = useMemo(() => {
        if (!nodesConfig) return {};
        return Object.fromEntries(
            Object.entries(nodesConfig).map(([nodeType, config]) => [
                nodeType,
                config.display
            ])
        );
    }, [nodesConfig]);

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

    const canCastType = useMemo(() => {
        return (sourceType, targetType) => {
            // Exact match
            if (sourceType === targetType) return true;

            // DEFAULT can connect to anything
            if (sourceType === 'DEFAULT' || targetType === 'DEFAULT') return true;

            // Check casting rules
            const allowedTargets = typeCastingRules[sourceType] || [];
            const canCast = allowedTargets.includes(targetType);

            if (!canCast && sourceType && targetType) {
                console.log(`Cannot cast ${sourceType} to ${targetType}`);
            }

            return canCast;
        };
    }, [typeCastingRules]);

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
        nodesConfig,

        isLoading,
        error,
        isReady: ! isLoading && !error && !!nodesConfig,

        nodeReactComponents,
        nodeDisplayInfo,
        getHandleType,
        getDefaultInputs,
        canCastType,

        nodesGroupedByCategory
    };
}