import React, { createContext, useState, useEffect, useContext } from 'react';
import { sceneApi } from '../../utils/api.js';

const SceneContext = createContext();

export function useScene() {
  return useContext(SceneContext);
}

export function SceneProvider({ children }) {
  const [sceneId, setSceneId] = useState(null);
  const [isSceneLoading, setIsSceneLoading] = useState(true);
  const [sceneError, setSceneError] = useState(null);

  // Check for an existing scene ID in session storage
  useEffect(() => {
    const storedSceneId = sessionStorage.getItem('mypixel_scene_id');

    if (storedSceneId) {
      console.log('Using existing scene:', storedSceneId);
      setSceneId(storedSceneId);
      setIsSceneLoading(false);
    } else {
      createNewScene();
    }
  }, []);

  // Create a new scene
  const createNewScene = async () => {
    setIsSceneLoading(true);
    setSceneError(null);

    try {
      const sceneData = await sceneApi.create();
      console.log('Created new scene:', sceneData.id);

      // Store the scene ID in session storage
      sessionStorage.setItem('mypixel_scene_id', sceneData.id);
      setSceneId(sceneData.id);
    } catch (error) {
      console.error('Scene creation error:', error);
      setSceneError(error.message);
    } finally {
      setIsSceneLoading(false);
    }
  };

  const value = {
    sceneId,
    isSceneLoading,
    sceneError,
    createNewScene,
  };

  return (
      <SceneContext.Provider value={value}>
        {children}
      </SceneContext.Provider>
  );
}