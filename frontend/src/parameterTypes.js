export const getHandleParameterType = (nodeType, handleId, handleType) => {
    const typeMap = {
      FloorNode: {
        number: { source: 'INT', target: 'INT' },
      },
      InputNode: {
        files: { source: 'FILENAMES_ARRAY', target: 'FILENAMES_ARRAY' },
      },
      // Add other node types as needed
      CombineNode: {
        files_0: { target: 'FILENAMES_ARRAY' },
        files_1: { target: 'FILENAMES_ARRAY' },
        files_2: { target: 'FILENAMES_ARRAY' },
        files_3: { target: 'FILENAMES_ARRAY' },
        files: { source: 'FILENAMES_ARRAY' },
      },
      OutputNode: {
        files: { target: 'FILENAMES_ARRAY' },
      },
    };
  
    return typeMap[nodeType]?.[handleId]?.[handleType] || null;
  };