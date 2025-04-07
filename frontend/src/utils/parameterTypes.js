
export const getHandleParameterType = (nodeType, handleId, handleType) => {
    const typeMap = {
      FloorNode: {
        number: { source: 'DOUBLE', target: 'DOUBLE' },
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
        files_4: { target: 'FILENAMES_ARRAY' },
        files: { source: 'FILENAMES_ARRAY' },
      },
      OutputNode: {
        files: { target: 'FILENAMES_ARRAY' },
      },
      GaussianBlurNode: {
        files: { target: 'FILENAMES_ARRAY', source: 'FILENAMES_ARRAY' },
        sizeX: { target: 'INT'},
        sizeY: { target: 'INT'},
        sigmaX: { target: 'DOUBLE'},
        sigmaY: { target: 'DOUBLE'}
      },
      S3InputNode: {
        files: { source: 'FILENAMES_ARRAY' },
        access_key_id: { target: 'STRING'},
        secret_access_key: { target: 'STRING'},
        region: { target: 'STRING'},
        bucket: { target: 'STRING'},
      }
    };
  
    return typeMap[nodeType]?.[handleId]?.[handleType] || null;
  };