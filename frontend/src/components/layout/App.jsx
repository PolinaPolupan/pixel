// Update your App.jsx
import React, { useRef, useEffect } from 'react';
import { ReactFlowProvider } from '@xyflow/react';
import DockLayout from 'rc-dock';
import AppContent from './AppContent.jsx';
import FileExplorer from '../file/FileExplorer.jsx';
import NodeTypesPanel from '../ui/NodeTypesPanel.jsx';
import ExecutionsPanel from '../execution/ExecutionPanel.jsx'; // Add this
import {NotificationProvider} from '../../services/contexts/NotificationContext.jsx';
import {ProgressProvider} from "../../services/contexts/ProgressContext.jsx";
import ErrorBoundary from "../ui/ErrorBoundary.jsx";

const defaultLayout = {
    dockbox: {
        mode:  'horizontal',
        children:  [
            {
                tabs:  [
                    {
                        id: 'flowCanvas',
                        title: 'Flow Canvas',
                        content: <AppContent />,
                        group:  'canvas',
                    },
                ],
                size: 70, // Reduced from 80
            },
            {
                size: 30, // Increased from 20
                mode: 'vertical',
                children: [
                    {
                        size: 33, // Split into 3 equal parts
                        tabs: [
                            {
                                id:  'fileExplorer',
                                title: 'File Explorer',
                                content: <FileExplorer />,
                            },
                        ],
                    },
                    {
                        size: 33,
                        tabs: [
                            {
                                id: 'nodeTypes',
                                title: 'Node Types',
                                content:  <NodeTypesPanel />,
                            },
                        ],
                    },
                    {
                        size: 34, // Add executions panel
                        tabs: [
                            {
                                id: 'executions',
                                title: 'Executions',
                                content: <ExecutionsPanel />,
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
        if (layoutRef. current) {
            const saved = localStorage.getItem('dockLayout');
            if (saved) {
                try {
                    layoutRef.current.loadLayout(JSON.parse(saved));
                } catch (e) {
                    console.error('Failed to load saved layout:', e);
                }
            }
        }
    }, []);

    const saveLayout = () => {
        if (layoutRef.current) {
            const saved = layoutRef.current.saveLayout();
            localStorage.setItem('dockLayout', JSON.stringify(saved));
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
                canvas:  { floatable: true, maximizable: true },
                explorer: { floatable: true, maximizable:  true },
            }}
            dropMode="all"
            onLayoutChange={saveLayout}
        />
    );
}

export default function App() {
    return (
        <ErrorBoundary>
            <div style={{ height:  '100vh', position: 'relative' }}>
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