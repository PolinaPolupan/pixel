import { useMemo } from 'react';
import { useNodesConfig } from '../utils/NodesConfig.jsx';

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

    // Group nodes by category
    const nodesByCategory = useMemo(() => {
        if (!nodeTypeDetails || Object.keys(nodeTypeDetails).length === 0) {
            return {};
        }

        return Object.entries(nodeTypeDetails).reduce((grouped, [type, details]) => {
            // Use specified category or "Other" as fallback
            const category = details.category || 'Other';
            if (!grouped[category]) {
                grouped[category] = [];
            }
            grouped[category].push({ type, details });
            return grouped;
        }, {});
    }, [nodeTypeDetails]);

    // Get sorted categories
    const sortedCategories = useMemo(() => {
        return Object.keys(nodesByCategory).sort();
    }, [nodesByCategory]);

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

    // Filter nodes by search term and category
    const filterNodes = useMemo(() => {
        return (searchTerm, activeCategory = 'All') => {
            // First apply search filter if any
            let filteredResults = { ...nodesByCategory };

            if (searchTerm && searchTerm.trim() !== '') {
                const searchLower = searchTerm.toLowerCase();
                filteredResults = {};

                Object.entries(nodesByCategory).forEach(([category, nodes]) => {
                    const filteredNodes = nodes.filter(({ type, details }) =>
                        type.toLowerCase().includes(searchLower) ||
                        details.description?.toLowerCase().includes(searchLower)
                    );

                    if (filteredNodes.length > 0) {
                        filteredResults[category] = filteredNodes;
                    }
                });
            }

            // Then apply category filter if not 'All'
            if (activeCategory !== 'All') {
                return {
                    [activeCategory]: filteredResults[activeCategory] || []
                };
            }

            return filteredResults;
        };
    }, [nodesByCategory]);

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

        // Category organization
        nodesByCategory,
        sortedCategories,
        filterNodes,

        // Utility to check if configs are ready
        isReady: !isLoading && !error && !!nodesConfig
    };
}