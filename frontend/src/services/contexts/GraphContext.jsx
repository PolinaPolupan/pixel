import React, { createContext, useState, useEffect, useContext } from 'react';
import { graphApi } from '../api.js';

const GraphContext = createContext();

export function useGraph() {
  return useContext(GraphContext);
}

export function GraphProvider({ children }) {
  const [graphId, setGraphId] = useState(null);
  const [isGraphLoading, setIsGraphLoading] = useState(true);
  const [graphError, setGraphError] = useState(null);

  // Check for an existing scene ID in session storage
  useEffect(() => {
    const storedGraphId = sessionStorage.getItem('graph_id');

    if (storedGraphId) {
      console.log('Using existing graph:', storedGraphId);
      setGraphId(storedGraphId);
      setIsGraphLoading(false);
    } else {
      createNewGraph();
    }
  }, []);

  const createNewGraph = async () => {
    setIsGraphLoading(true);
    setGraphError(null);

    try {
      const graphData = await graphApi.create();
      console.log('Created new graph:', graphData.id);

      sessionStorage.setItem('graph_id', graphData.id);
      setGraphId(graphData.id);
    } catch (error) {
      console.error('Graph creation error:', error);
      setGraphError(error.message);
    } finally {
      setIsGraphLoading(false);
    }
  };

  const value = {
    graphId,
    isGraphLoading,
    graphError,
    createNewGraph,
  };

  return (
      <GraphContext.Provider value={value}>
        {children}
      </GraphContext.Provider>
  );
}