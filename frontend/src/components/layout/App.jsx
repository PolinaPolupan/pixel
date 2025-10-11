import React, { useRef, useEffect } from 'react';
import { ReactFlowProvider } from '@xyflow/react';
import DockLayout from 'rc-dock';
import AppContent from './AppContent.jsx';
import FileExplorer from '../file/FileExplorer.jsx';
import NodeTypesPanel from '../ui/NodeTypesPanel.jsx';
import {NotificationProvider} from '../../services/contexts/NotificationContext.jsx';
import {ProgressProvider} from "../../services/contexts/ProgressContext.jsx";
import ErrorBoundary from "../ui/ErrorBoundary.jsx";


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
    const layoutRef = useRef(null);

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
                        <ProgressProvider>
                            <AppWithSceneContext />
                        </ProgressProvider>
                    </NotificationProvider>
                </ReactFlowProvider>
            </div>
        </ErrorBoundary>
    );
}