import { useMemo } from 'react';
import { useNodesConfig } from '../services/NodesConfig.jsx';
import Node from "../components/graph/Node.jsx";

export function useNodesApi() {
    const { nodesConfig, isLoading, error } = useNodesConfig();

    const typeCastingRules = useMemo(() => ({
        'INT': ['FLOAT', 'DOUBLE'],
        'FLOAT': ['DOUBLE', 'INT'],
        'DOUBLE': ['FLOAT', 'INT'],
        'STRING': [],
        'FILEPATH_ARRAY': [],
        'STRING_ARRAY': [],
        'VECTOR2D': []
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
            if (sourceType === targetType) return true;

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
        isReady: !isLoading && !error && !!nodesConfig,

        nodeReactComponents,
        nodeDisplayInfo,
        getHandleType,
        getDefaultInputs,
        canCastType,

        nodesGroupedByCategory
    };
}