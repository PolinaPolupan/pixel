import { nodesConfig } from './NodesConfig';

const typeCastingRules = {
  'INT': ['FLOAT', 'DOUBLE'],
  'FLOAT': ['DOUBLE', 'INT'],
  'DOUBLE': ['FLOAT', 'INT'],
  'STRING': [],
  'FILENAMES_ARRAY': [],
  'STRING_ARRAY': [],
  'VECTOR2D': []
};

export const getHandleParameterType = (nodeType, handleId, handleType) => {
  return nodesConfig[nodeType]?.handles?.[handleId]?.[handleType] || null;
};

export const canCastType = (sourceType, targetType) => {
  if (sourceType === targetType) return true;

  const allowedTargets = typeCastingRules[sourceType] || [];
  const canCast = allowedTargets.includes(targetType);

  if (!canCast && sourceType && targetType) {
    console.log(`Cannot cast ${sourceType} to ${targetType}`);
  }

  return canCast;
};