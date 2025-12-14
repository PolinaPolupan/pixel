import React, { useRef, useEffect, useCallback } from 'react';
import { ReactFlowProvider } from '@xyflow/react';
import DockLayout from 'rc-dock';
import AppContent from './AppContent.jsx';
import FileExplorer from '../file/FileExplorer.jsx';
import NodeTypesPanel from '../ui/NodeTypesPanel.jsx';
import ExecutionsPanel from '../execution/ExecutionPanel.jsx';
import ConnectionsPanel from '../connection/ConnectionsPanel.jsx';  // ← Import
import {NotificationProvider} from '../../services/contexts/NotificationContext.jsx';
import {ProgressProvider} from "../../services/contexts/ProgressContext.jsx";
import ErrorBoundary from "../ui/ErrorBoundary.jsx";

function AppWithSceneContext() {
    const layoutRef = useRef(null);

    const openNodeFiles = useCallback((graphExecutionId, nodeId) => {
        if (! layoutRef.current) return;

        const tabId = `node-files-${graphExecutionId}-${nodeId}`;
        const tabTitle = `Node #${nodeId} Files`;

        const existingTab = layoutRef.current.find(tabId);
        if (existingTab) {
            layoutRef.current.dockMove(existingTab, null, 'front');
            return;
        }

        const newTab = {
            id: tabId,
            title: tabTitle,
            content: (
                <FileExplorer
                    graphExecutionId={graphExecutionId}
                    nodeId={nodeId}
                    key={tabId}
                />
            ),
            closable: true,
        };

        layoutRef.current.dockMove(newTab, 'fileExplorer', 'after-tab');
    }, []);

    const defaultLayout = {
        dockbox: {
            mode: 'vertical',
            children: [
                {
                    mode: 'horizontal',
                    size: 70,
                    children: [
                        {
                            tabs:  [
                                {
                                    id: 'flowCanvas',
                                    title: 'Flow Canvas',
                                    content: <AppContent />,
                                    group: 'canvas',
                                },
                            ],
                            size: 70,
                        },
                        {
                            size: 30,
                            mode: 'vertical',
                            children: [
                                {
                                    size: 33,  // ← Changed from 50 to make room
                                    tabs: [
                                        {
                                            id: 'nodeTypes',
                                            title: 'Node Types',
                                            content: <NodeTypesPanel />,
                                        },
                                    ],
                                },
                                {
                                    size: 33,  // ← Changed from 50
                                    tabs: [
                                        {
                                            id: 'executions',
                                            title: 'Executions',
                                            content:  <ExecutionsPanel onViewFiles={openNodeFiles} />,
                                        },
                                    ],
                                },
                                {
                                    size: 34,  // ← New section for Connections
                                    tabs: [
                                        {
                                            id: 'connections',
                                            title: 'Connections',
                                            content:  <ConnectionsPanel />,
                                        },
                                    ],
                                },
                            ],
                        }
                    ],
                },
                {
                    size: 30,
                    tabs: [
                        {
                            id: 'fileExplorer',
                            title: 'File Explorer',
                            content: <FileExplorer />,
                        },
                    ],
                }
            ],
        },
    };

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
            <div style={{ height: '100vh', position:  'relative' }}>
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