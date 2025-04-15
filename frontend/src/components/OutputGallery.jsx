import React, { useState, useEffect } from 'react';
import { useScene } from './SceneContext';

function OutputGallery() {
  const { sceneId } = useScene();
  const [outputImages, setOutputImages] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [isGalleryOpen, setIsGalleryOpen] = useState(false);
  const [selectedImage, setSelectedImage] = useState(null);
  const [refreshCounter, setRefreshCounter] = useState(0); // For cache busting

  // Load output images when component mounts or scene changes
  useEffect(() => {
    if (isGalleryOpen && sceneId) {
      loadOutputImages();
    }
  }, [sceneId, isGalleryOpen, refreshCounter]);

  // Function to load output images
  const loadOutputImages = async () => {
    setIsLoading(true);
    setError(null);
    
    try {
      // Add a cache-busting query parameter to prevent browser caching
      const timestamp = new Date().getTime();
      const response = await fetch(`http://localhost:8080/v1/scene/${sceneId}/output?t=${timestamp}`, {
        credentials: 'include',
        headers: {
          // These headers tell the browser not to use the cache
          'Cache-Control': 'no-cache, no-store, must-revalidate',
          'Pragma': 'no-cache',
          'Expires': '0'
        }
      });
      
      if (!response.ok) {
        throw new Error(`Failed to load output images: ${response.statusText}`);
      }
      
      const imageUrls = await response.json();
      
      // Map the URLs to image objects with cache-busting query parameters
      const images = imageUrls.map(url => {
        const urlObj = new URL(url);
        const filename = decodeURIComponent(urlObj.pathname.split('/').pop());
        
        // Add a timestamp to the URL to prevent browser caching
        const cacheBustUrl = `${url}${url.includes('?') ? '&' : '?'}t=${timestamp}`;
        
        return {
          url: cacheBustUrl, // Use cache-busted URL for display
          originalUrl: url,  // Keep original URL for downloads
          filename,
          id: `output-${filename}-${timestamp}` // Unique ID that changes on refresh
        };
      });
      
      setOutputImages(images);
    } catch (err) {
      console.error('Error loading output images:', err);
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  // Function to trigger a refresh
  const handleRefresh = () => {
    // Increment counter to force re-fetch
    setRefreshCounter(prev => prev + 1);
  };

  // Function to download all output images as ZIP
  const downloadAllAsZip = () => {
    if (!sceneId || outputImages.length === 0) return;
    
    // Create a link to download the ZIP and trigger it
    const downloadLink = document.createElement('a');
    // Add timestamp to prevent caching
    const timestamp = new Date().getTime();
    downloadLink.href = `http://localhost:8080/v1/scene/${sceneId}/output/download-zip?t=${timestamp}`;
    downloadLink.download = `output-${sceneId.substring(0, 8)}.zip`;
    document.body.appendChild(downloadLink);
    downloadLink.click();
    document.body.removeChild(downloadLink);
  };

  // Function to open an image in a modal
  const openImageModal = (image) => {
    setSelectedImage(image);
  };

  // Function to close the image modal
  const closeImageModal = () => {
    setSelectedImage(null);
  };

  // Toggle the gallery
  const toggleGallery = () => {
    const newState = !isGalleryOpen;
    setIsGalleryOpen(newState);
    if (newState) {
      // Force a refresh when opening the gallery
      handleRefresh();
    }
  };

  return (
    <div className="output-gallery" style={{
      position: 'relative',
      fontFamily: 'Arial, sans-serif',
      color: 'white',
    }}>
      {/* Gallery toggle button */}
      <button 
        onClick={toggleGallery}
        style={{
          padding: '8px 12px',
          background: '#3c4fe0',
          color: 'white',
          border: 'none',
          borderRadius: '4px',
          display: 'flex',
          alignItems: 'center',
          gap: '6px',
          cursor: 'pointer',
          fontSize: '13px',
        }}
      >
        <span>{isGalleryOpen ? 'Hide Output Gallery' : 'View Output Gallery'}</span>
        <span style={{ fontSize: '10px' }}>{isGalleryOpen ? '▲' : '▼'}</span>
      </button>

      {/* Gallery Panel */}
      {isGalleryOpen && (
        <div style={{
          position: 'absolute',
          top: '100%',
          right: '0',
          width: '400px',
          maxHeight: '500px',
          marginTop: '8px',
          background: 'rgba(30, 30, 30, 0.95)',
          borderRadius: '4px',
          padding: '16px',
          boxShadow: '0 4px 20px rgba(0, 0, 0, 0.3)',
          zIndex: 1000,
          display: 'flex',
          flexDirection: 'column',
        }}>
          <div style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: '16px',
          }}>
            <h3 style={{ margin: 0, fontSize: '16px' }}>Output Images</h3>
            <div style={{ display: 'flex', gap: '8px' }}>
              <button 
                onClick={handleRefresh}
                disabled={isLoading}
                style={{
                  padding: '4px 8px',
                  background: 'rgba(255, 255, 255, 0.1)',
                  border: 'none',
                  borderRadius: '3px',
                  color: 'white',
                  fontSize: '12px',
                  cursor: isLoading ? 'not-allowed' : 'pointer',
                }}
              >
                {isLoading ? 'Loading...' : 'Refresh'}
              </button>
              <button 
                onClick={downloadAllAsZip}
                disabled={outputImages.length === 0 || isLoading}
                style={{
                  padding: '4px 8px',
                  background: outputImages.length === 0 ? 'rgba(255, 255, 255, 0.1)' : '#3c4fe0',
                  border: 'none',
                  borderRadius: '3px',
                  color: 'white',
                  fontSize: '12px',
                  cursor: outputImages.length === 0 ? 'not-allowed' : 'pointer',
                }}
              >
                Download All (ZIP)
              </button>
            </div>
          </div>

          {/* Error message */}
          {error && (
            <div style={{
              padding: '8px 12px',
              background: 'rgba(220, 53, 69, 0.2)',
              color: '#ff6b6b',
              borderRadius: '3px',
              marginBottom: '16px',
              fontSize: '13px',
            }}>
              {error}
            </div>
          )}

          {/* Loading indicator */}
          {isLoading && (
            <div style={{
              padding: '20px 0',
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'center',
            }}>
              <div style={{
                width: '24px',
                height: '24px',
                border: '3px solid rgba(255, 255, 255, 0.1)',
                borderRadius: '50%',
                borderTopColor: '#3c4fe0',
                animation: 'spin 1s linear infinite',
              }} />
              <style>{`
                @keyframes spin {
                  to { transform: rotate(360deg); }
                }
              `}</style>
            </div>
          )}

          {/* Image grid */}
          {!isLoading && outputImages.length === 0 ? (
            <div style={{
              padding: '30px 0',
              textAlign: 'center',
              color: 'rgba(255, 255, 255, 0.6)',
              fontSize: '14px',
            }}>
              No output images available yet.
            </div>
          ) : (
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(3, 1fr)',
              gap: '8px',
              overflowY: 'auto',
              maxHeight: '350px',
              padding: '4px',
            }}>
              {outputImages.map(image => (
                <div 
                  key={image.id} 
                  onClick={() => openImageModal(image)}
                  style={{
                    position: 'relative',
                    aspectRatio: '1/1',
                    overflow: 'hidden',
                    borderRadius: '3px',
                    cursor: 'pointer',
                  }}
                >
                  <img 
                    src={image.url} 
                    alt={image.filename}
                    style={{
                      width: '100%',
                      height: '100%',
                      objectFit: 'cover',
                    }} 
                  />
                  <div style={{
                    position: 'absolute',
                    bottom: '0',
                    left: '0',
                    right: '0',
                    padding: '4px 6px',
                    background: 'rgba(0, 0, 0, 0.6)',
                    fontSize: '10px',
                    whiteSpace: 'nowrap',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                  }}>
                    {image.filename}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Image Modal */}
      {selectedImage && (
        <div style={{
          position: 'fixed',
          top: '0',
          left: '0',
          right: '0',
          bottom: '0',
          background: 'rgba(0, 0, 0, 0.9)',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 2000,
          padding: '20px',
        }} onClick={closeImageModal}>
          <div style={{
            position: 'relative',
            maxWidth: '90%',
            maxHeight: '80vh',
            borderRadius: '4px',
            overflow: 'hidden',
            boxShadow: '0 10px 30px rgba(0, 0, 0, 0.5)',
          }} onClick={e => e.stopPropagation()}>
            <img 
              src={selectedImage.url} 
              alt={selectedImage.filename}
              style={{
                maxWidth: '100%',
                maxHeight: '80vh',
                display: 'block',
              }} 
            />
          </div>
          
          <div style={{
            marginTop: '20px',
            display: 'flex',
            gap: '10px',
          }}>
            <a 
              href={selectedImage.originalUrl || selectedImage.url}
              download={selectedImage.filename}
              style={{
                padding: '8px 16px',
                background: '#3c4fe0',
                color: 'white',
                borderRadius: '4px',
                textDecoration: 'none',
                fontSize: '14px',
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
              }}
              onClick={e => e.stopPropagation()}
            >
              <span>Download</span>
            </a>
            <button 
              onClick={closeImageModal}
              style={{
                padding: '8px 16px',
                background: 'rgba(255, 255, 255, 0.2)',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                fontSize: '14px',
                cursor: 'pointer',
              }}
            >
              Close
            </button>
          </div>
          
          <div style={{
            marginTop: '10px',
            fontSize: '14px',
            color: 'rgba(255, 255, 255, 0.8)',
          }}>
            {selectedImage.filename}
          </div>
        </div>
      )}
    </div>
  );
}

export default OutputGallery;