import React from 'react';
import { useReactFlow } from '@xyflow/react';
import GraphDataTransformer from '../graph/GraphDataTransformer.jsx';

const DebugPanel = () => {
    const { getNodes, getEdges } = useReactFlow();

    // Get current nodes and edges
    const nodes = getNodes();
    const edges = getEdges();

    return (
        <div
            style={{
                top: 10,
                right: 10,
                width: '300px',
                maxHeight: '80vh',
                overflowY: 'auto',

                border: '1px solid #ccc',
                borderRadius: '5px',
                padding: '10px',
                boxShadow: '0 2px 5px rgba(0,0,0,0.2)',
                fontFamily: 'monospace',
                fontSize: '12px',
                zIndex: 1000,
            }}
        >
            <h3>Debug Panel</h3>
            <GraphDataTransformer />

            <section>
                <h4>Nodes ({nodes.length})</h4>
                <pre>
          {JSON.stringify(
              nodes.map(node => ({
                  id: node.id,
                  type: node.type,
                  position: node.position,
                  data: node.data,
              })),
              null,
              2
          )}
        </pre>
            </section>

            <section>
                <h4>Edges ({edges.length})</h4>
                <pre>
          {JSON.stringify(
              edges.map(edge => ({
                  id: edge.id,
                  source: edge.source,
                  target: edge.target,
                  type: edge.type,
              })),
              null,
              2
          )}
        </pre>
            </section>
        </div>
    );
};

export default DebugPanel;