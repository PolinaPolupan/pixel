import { useState } from 'react';

function ImageUpload({ onImagesSelected, maxImages = 10, nodeId, initialImages = [] }) {
  const [images, setImages] = useState(initialImages);
  const [isUploading, setIsUploading] = useState(false);

  // Handle local file upload and send to server
  const handleFileChange = async (event) => {
    const files = Array.from(event.target.files);
    const imageFiles = files.filter(file => file.type.startsWith('image/'));

    if (imageFiles.length === 0) {
      setIsUploading(false);
      return; // No images to upload
    }

    setIsUploading(true);
    try {
      // Create a single FormData for all files
      const formData = new FormData();
      
      // Create an array to store file preview data
      const filePreviewPromises = imageFiles.map(file => {
        // Add each file to the FormData with unique keys
        formData.append('file', file);
        
        // Return a promise that resolves with the preview data
        return new Promise((resolve, reject) => {
          const reader = new FileReader();
          reader.onload = () => {
            resolve({
              id: Date.now() + Math.random().toString(36).substr(2, 9),
              name: file.name,
              url: reader.result, // Local data URL for preview
              serverUrl: `${file.name}`, // Assumed server URL
              file,
            });
          };
          reader.onerror = () => reject(new Error(`Failed to read file ${file.name}`));
          reader.readAsDataURL(file);
        });
      });

      // Wait for all file previews to be generated
      const filePreviewsData = await Promise.all(filePreviewPromises);
      
      // Make a single request to upload all files at once
      const response = await fetch('http://localhost:8080/v1/image/', {
        method: 'POST',
        body: formData,
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`Upload failed: ${response.statusText}`);
      }

      console.log(`Uploaded ${imageFiles.length} files successfully with status ${response.status}`);
      
      // Update the state with the new images
      const updatedImages = [...images, ...filePreviewsData].slice(0, maxImages);
      setImages(updatedImages);
      updateParent(updatedImages);
      
    } catch (error) {
      console.error('Failed to upload images:', error);
      alert(`Error uploading images: ${error.message}`);
    } finally {
      console.log('Finished uploading, resetting isUploading');
      setIsUploading(false);
    }
  };

  // Clear all images
  const clearImages = () => {
    setImages([]);
    updateParent([]);
  };

  // Update parent with server URLs for 'files' input
  const updateParent = (imageList) => {
    if (onImagesSelected) {
      const fileUrls = imageList.map(image => image.serverUrl);
      onImagesSelected(fileUrls);
    }
  };

  // Format image count label
  const getImageLabel = () => {
    if (images.length === 0) return "No images";
    if (images.length === 1) return "1 image";
    return `${images.length} images`;
  };

  return (
    <div style={{
      fontFamily: 'Arial, sans-serif',
      fontSize: '12px',
      padding: '8px',
      borderRadius: '4px',
      background: 'rgba(0, 0, 0, 0)',
      maxWidth: '250px',
    }}>
      {/* Header with count and clear button */}
      {images.length > 0 && (
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '6px',
        }}>
          <span style={{ fontWeight: 500 }}>{getImageLabel()}</span>
          <button 
            onClick={clearImages}
            disabled={isUploading}
            style={{
              padding: '2px 6px',
              fontSize: '10px',
              background: 'rgba(0, 0, 0, 0)',
              color: isUploading ? 'rgba(255, 255, 255, 0.3)' : '#d9534f',
              border: `1px solid ${isUploading ? 'rgba(255, 255, 255, 0.2)' : 'rgba(217, 83, 79, 0.5)'}`,
              borderRadius: '2px',
              cursor: isUploading ? 'not-allowed' : 'pointer',
              transition: 'background 0.2s, color 0.2s, border-color 0.2s',
            }}
            onMouseOver={(e) => !isUploading && (e.target.style.background = 'rgba(217, 83, 79, 0.1)', e.target.style.borderColor = '#d9534f')}
            onMouseOut={(e) => !isUploading && (e.target.style.background = 'rgba(0, 0, 0, 0)', e.target.style.borderColor = 'rgba(217, 83, 79, 0.5)')}
          >
            Clear
          </button>
        </div>
      )}

      {/* Image previews */}
      {images.length > 0 && (
        <div style={{
          display: 'flex',
          gap: '4px',
          marginBottom: '8px',
        }}>
          {images.slice(0, 3).map(image => (
            <div key={image.id} style={{
              width: '36px',
              height: '36px',
            }}>
              <img 
                src={image.url}
                alt={image.name}
                title={image.name}
                style={{
                  width: '100%',
                  height: '100%',
                  objectFit: 'cover',
                  border: '1px solid rgba(255, 255, 255, 0.2)',
                  borderRadius: '2px',
                }}
              />
            </div>
          ))}
          {images.length > 3 && (
            <div style={{
              width: '36px',
              height: '36px',
              background: 'rgba(255, 255, 255, 0.05)',
              border: '1px solid rgba(255, 255, 255, 0.2)',
              borderRadius: '2px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '10px',
            }}>
              +{images.length - 3}
            </div>
          )}
        </div>
      )}

      {/* Upload button */}
      <div style={{
        display: 'flex',
        gap: '6px',
      }}>
        <label style={{
          display: 'inline-block',
          padding: '4px 8px',
          background: 'rgba(0, 0, 0, 0)',
          border: `1px solid ${isUploading ? 'rgba(255, 255, 255, 0.2)' : 'rgba(255, 255, 255, 0.3)'}`,
          borderRadius: '2px',
          cursor: isUploading ? 'not-allowed' : 'pointer',
          textAlign: 'center',
          transition: 'border-color 0.2s',
        }}
        onMouseOver={(e) => !isUploading && (e.target.style.borderColor = 'rgba(255, 255, 255, 0.5)')}
        onMouseOut={(e) => !isUploading && (e.target.style.borderColor = 'rgba(255, 255, 255, 0.3)')}
        >
          {isUploading ? 'Uploading...' : (images.length > 0 ? 'Add Images' : 'Upload Images')}
          <input
            type="file"
            accept="image/*"
            multiple
            onChange={handleFileChange}
            disabled={isUploading}
            style={{ display: 'none' }}
          />
        </label>
      </div>
    </div>
  );
}

export default ImageUpload;