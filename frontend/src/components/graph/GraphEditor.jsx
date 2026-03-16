import {Background, ReactFlow} from "@xyflow/react";

export function GraphEditor({
                                nodes,
                                edges,
                                nodeTypes,
                                onNodesChange,
                                onEdgesChange,
                                onConnect,
                                isValidConnection,
                                colorMode
                            }) {
    return (
        <ReactFlow
            nodes={nodes}
            edges={edges}
            nodeTypes={nodeTypes}
            onNodesChange={onNodesChange}
            onConnect={onConnect}
            isValidConnection={isValidConnection}
            onEdgesChange={onEdgesChange}
            colorMode={colorMode}                    // <- this is now "dark" or "light"
            className={colorMode}                    // <- adds .dark or .light to container
            style={{ width: '100%', height: '100%' }}
            fitView
        >
            <Background variant="dots" gap={12} size={1} />
        </ReactFlow>
    );
}