export const getParameterColor = (parameterType) => {
    const colorMap = {
      FLOAT: '#ff6b6b', // Reddish
      INT: '#4ecdc4', // Turquoise
      DOUBLE: '#45b7d1', // Blue
      STRING: '#96ceb4', // Greenish
      STRING_ARRAY: '#ffeead', // Yellowish
      FILENAMES_ARRAY: '#d4a5a5', // Pinkish
    };
    return colorMap[parameterType] || '#cccccc'; // Default gray if unknown
  };