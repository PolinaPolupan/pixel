import React from 'react';
import { ReactFlowProvider } from '@xyflow/react';
import { SceneProvider } from './components/SceneContext';
import AppContent from './AppContent';
import LoadingScreen from './components/LoadingScreen';
import ErrorScreen from './components/ErrorScreen';
import { useScene } from './components/SceneContext';

// Separate component for loading and error handling
function AppWithSceneContext() {
  const { isSceneLoading, sceneError } = useScene();

  if (isSceneLoading) {
    return <LoadingScreen message="Initializing your workspace..." />;
  }

  if (sceneError) {
    return <ErrorScreen message={sceneError} />;
  }

  return <AppContent />;
}

export default function App() {
  return (
    <ReactFlowProvider>
      <SceneProvider>
        <AppWithSceneContext />
      </SceneProvider>
    </ReactFlowProvider>
  );
}