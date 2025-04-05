import { useState } from 'react';

function ImageUpload({ onImagesSelected, maxImages = 10 }) {
  const [images, setImages] = useState([]);
  
  const handleFileChange = (event) => {
    const files = Array.from(event.target.files);
    const imageFiles = files.filter(file => file.type.startsWith('image/'));
    
    // Process each new image file
    const processPromises = imageFiles.map(file => {
      return new Promise(resolve => {
        const reader = new FileReader();
        reader.onload = () => {
          resolve({
            id: Date.now() + Math.random().toString(36).substr(2, 9),
            name: file.name,
            url: reader.result,
            file
          });
        };
        reader.readAsDataURL(file);
      });
    });
    
    Promise.all(processPromises).then(newImageObjects => {
      const updatedImages = [...images, ...newImageObjects].slice(0, maxImages);
      setImages(updatedImages);
      
      if (onImagesSelected) {
        onImagesSelected(updatedImages);
      }
    });
  };
  
  // Format image count label
  const getImageLabel = () => {
    if (images.length === 0) return "No images";
    if (images.length === 1) return "1 image";
    return `${images.length} images`;
  };
  
  // Clear all images
  const clearImages = () => {
    setImages([]);
    if (onImagesSelected) {
      onImagesSelected([]);
    }
  };
  
  return (
    <div style={{ fontSize: '12px' }}>
      {/* Image count and clear button */}
      {images.length > 0 && (
        <div style={{ 
          display: 'flex', 
          justifyContent: 'space-between',
          alignItems: 'center', 
          marginBottom: '5px' 
        }}>
          <div>{getImageLabel()}</div>
          <button 
            onClick={clearImages}
            style={{
              fontSize: '10px',
              padding: '2px 5px',
              background: '#f0f0f0',
              border: '1px solid #ccc',
              borderRadius: '3px',
              cursor: 'pointer'
            }}
          >
            Clear
          </button>
        </div>
      )}
      
      {/* Preview of first 3 images */}
      {images.length > 0 && (
        <div style={{ 
          display: 'flex', 
          gap: '4px', 
          marginBottom: '6px'
        }}>
          {images.slice(0, 3).map(image => (
            <div 
              key={image.id} 
              style={{ 
                width: '36px', 
                height: '36px',
                flexShrink: 0
              }}
            >
              <img 
                src={image.url} 
                alt=""
                style={{ 
                  width: '100%', 
                  height: '100%', 
                  objectFit: 'cover',
                  borderRadius: '3px'
                }} 
              />
            </div>
          ))}
          {images.length > 3 && (
            <div style={{ 
              width: '36px', 
              height: '36px',
              background: '#f0f0f0',
              borderRadius: '3px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '10px'
            }}>
              +{images.length - 3}
            </div>
          )}
        </div>
      )}
      
      {/* Upload button */}
      <label 
        style={{ 
          display: 'inline-block',
          padding: '4px 8px',
          backgroundColor: '#f0f0f0',
          border: '1px solid #ccc',
          borderRadius: '4px',
          cursor: 'pointer',
          fontSize: '12px',
          width: 'calc(100% - 16px)',
          textAlign: 'center'
        }}
      >
        {images.length > 0 ? 'Add More Images' : 'Upload Images'}
        <input
          type="file"
          accept="image/*"
          multiple
          onChange={handleFileChange}
          style={{ display: 'none' }}
        />
      </label>
    </div>
  );
}

export default ImageUpload;