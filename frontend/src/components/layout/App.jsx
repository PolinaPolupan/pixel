import React, { useRef, useEffect } from 'react';
import { ReactFlowProvider } from '@xyflow/react';
import DockLayout from 'rc-dock';
import { GraphProvider } from '../../services/contexts/GraphContext.jsx';
import AppContent from './AppContent.jsx';
import FileExplorer from '../file/FileExplorer.jsx';
import LoadingScreen from '../ui/LoadingScreen.jsx';
import { useGraph } from '../../services/contexts/GraphContext.jsx';
import NodeTypesPanel from '../ui/NodeTypesPanel.jsx';
import {NotificationProvider, useNotification} from '../../services/contexts/NotificationContext.jsx';
import {ProgressProvider} from "../../services/contexts/ProgressContext.jsx";
import ErrorBoundary from "../ui/ErrorBoundary.jsx";

// rc-dock layout with Flow Canvas (80%) and File Explorer (20%) split
const defaultLayout = {
    dockbox: {
        mode: 'horizontal',
        children: [
            {
                tabs: [
                    {
                        id: 'flowCanvas',
                        title: 'Flow Canvas',
                        content: <AppContent />,
                        group: 'canvas',
                    },
                ],
                size: 80,
            },
            {
                size: 20,
                mode: 'vertical',
                children: [
                    {
                        size: 50,
                        tabs: [
                            {
                                id: 'fileExplorer',
                                title: 'File Explorer',
                                content: <FileExplorer />,
                            },
                        ],
                    },
                    {
                        size: 50,
                        tabs: [
                            {
                                id: 'nodeTypes',
                                title: 'Node Types',
                                content: (
                                    <NodeTypesPanel />
                                ),
                            },
                        ],
                    },
                ],
            }
        ],
    },
};

function AppWithSceneContext() {
    const { isGraphLoading, graphError } = useGraph();
    const { setError } = useNotification();
    const layoutRef = useRef(null);

    // Persist and debug layout
    useEffect(() => {
        if (layoutRef.current) {
            const saved = localStorage.getItem('dockLayout');
            if (saved) {
                try {
                    layoutRef.current.loadLayout(JSON.parse(saved));
                } catch (e) {
                    console.error('Failed to load saved layout:', e);
                }
            }
            console.log('Initial rc-dock layout:', layoutRef.current.saveLayout());
        }
    }, []);

    const saveLayout = () => {
        if (layoutRef.current) {
            const saved = layoutRef.current.saveLayout();
            localStorage.setItem('dockLayout', JSON.stringify(saved));
            console.log('Saved rc-dock layout:', saved);
        }
    };

    useEffect(() => {
        if (graphError) {
            setError(graphError);
        }
    }, [graphError, setError]);

    if (isGraphLoading) {
        return <LoadingScreen message="Initializing your workspace..." />;
    }

    return (
        <DockLayout
            ref={layoutRef}
            defaultLayout={defaultLayout}
            style={{
                position: 'absolute',
                left: 0,
                top: 0,
                right: 0,
                bottom: 0,
            }}
            groups={{
                canvas: { floatable: true, maximizable: true },
                explorer: { floatable: true, maximizable: true },
            }}
            dropMode="all"
            onLayoutChange={saveLayout}
        />
    );
}

export default function App() {
    return (
        <ErrorBoundary>
            <div style={{ height: '100vh', position: 'relative' }}>
                <ReactFlowProvider>
                    <NotificationProvider>
                        <GraphProvider>
                            <ProgressProvider>
                                <AppWithSceneContext />
                            </ProgressProvider>
                        </GraphProvider>
                    </NotificationProvider>
                </ReactFlowProvider>
            </div>
        </ErrorBoundary>
    );
}