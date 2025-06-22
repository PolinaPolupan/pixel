import React, { useMemo } from 'react';
import { useNodesConfig } from './NodesConfig';


export function useHandleTypes() {
  const { nodesConfig, isLoading, error } = useNodesConfig();

  const typeCastingRules = {
    'INT': ['FLOAT', 'DOUBLE'],
    'FLOAT': ['DOUBLE', 'INT'],
    'DOUBLE': ['FLOAT', 'INT'],
    'STRING': [],
    'FILENAMES_ARRAY': [],
    'STRING_ARRAY': [],
    'VECTOR2D': []
  };

  const getHandleParameterType = useMemo(() => {
    return (nodeType, handleId, handleType) => {
      return nodesConfig[nodeType]?.handles?.[handleId]?.[handleType] || null;
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

  return {
    getHandleParameterType,
    canCastType,
    isLoading,
    error
  };
}