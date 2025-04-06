import { useState } from 'react';
import './ImageUpload.css';

function ImageUpload({ onImagesSelected, maxImages = 10, nodeId, initialImages = [] }) {
  const [images, setImages] = useState(initialImages);
  const [isUploading, setIsUploading] = useState(false);

  // Handle local file upload and send to server
  const handleFileChange = async (event) => {
    const files = Array.from(event.target.files);
    const imageFiles = files.filter(file => file.type.startsWith('image/'));

    setIsUploading(true);
    try {
      // Process local previews and upload concurrently
      const processPromises = imageFiles.map(file => {
        return new Promise((resolve) => {
          const reader = new FileReader();
          reader.onload = async () => {
            // Upload to server
            const formData = new FormData();
            formData.append('file', file);
            // const response = await fetch('/api/upload', {
            //   method: 'POST',
            //   body: formData,
            // });

            // if (!response.ok) throw new Error('Upload failed');
            // const result = await response.json(); // { name: "file.png", url: "..." }
            const result = { name: "file.png", url: "..." }

            resolve({
              id: Date.now() + Math.random().toString(36).substr(2, 9),
              name: result.name || file.name,
              url: reader.result, // Local data URL for preview
              serverUrl: result.url || file.name, // Server URL or filename for data.files
              file,
            });
          };
          reader.readAsDataURL(file);
        });
      });

      const newImageObjects = await Promise.all(processPromises);
      const updatedImages = [...images, ...newImageObjects].slice(0, maxImages);
      setImages(updatedImages);
      updateParent(updatedImages);
    } catch (error) {
      console.error('Failed to upload images:', error);
      alert('Error uploading images');
    } finally {
      setIsUploading(false);
    }
  };

  // Clear all images
  const clearImages = () => {
    setImages([]);
    updateParent([]);
  };

  // Update parent with server URLs (or filenames) for 'files' input
  const updateParent = (imageList) => {
    if (onImagesSelected) {
      const fileUrls = imageList.map(image => image.serverUrl); // Use server-provided URLs or filenames
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
    <div className="image-upload-container">
      {/* Header with count and clear button */}
      {images.length > 0 && (
        <div className="image-upload-header">
          <span className="image-count">{getImageLabel()}</span>
          <button 
            onClick={clearImages}
            className="clear-button"
            disabled={isUploading}
          >
            Clear
          </button>
        </div>
      )}

      {/* Image previews */}
      {images.length > 0 && (
        <div className="image-preview-grid">
          {images.slice(0, 3).map(image => (
            <div key={image.id} className="image-preview">
              <img 
                src={image.url} // Local data URL
                alt={image.name}
                title={image.name} // Show filename on hover
              />
            </div>
          ))}
          {images.length > 3 && (
            <div className="more-images">
              +{images.length - 3}
            </div>
          )}
        </div>
      )}

      {/* Upload button */}
      <div className="image-upload-actions">
        <label className="upload-button">
          {isUploading ? 'Uploading...' : (images.length > 0 ? 'Add Images' : 'Upload Images')}
          <input
            type="file"
            accept="image/*"
            multiple
            onChange={handleFileChange}
            disabled={isUploading}
          />
        </label>
      </div>
    </div>
  );
}

export default ImageUpload;