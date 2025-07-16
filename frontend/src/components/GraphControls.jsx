import React from 'react';
import { Panel } from '@xyflow/react';
import { PlayButton } from './PlayButton';
import ProgressBar from './ProgressBar';

export function GraphControls({
                                  handlePlay,
                                  isProcessing,
                                  configLoading
                              }) {
    return (
        <Panel position="bottom-center" style={{ margin: '16px' }}>
            <div style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: '12px',
                maxWidth: '400px',
                minHeight: '74px',
            }}>
                <div style={{ height: '40px', display: 'flex', alignItems: 'center' }}>
                    <PlayButton
                        onClick={handlePlay}
                        isProcessing={isProcessing}
                        disabled={configLoading}
                    />
                </div>
                <div style={{ height: '26px', width: '300px' }}>
                    <ProgressBar />
                </div>
            </div>
        </Panel>
    );
}