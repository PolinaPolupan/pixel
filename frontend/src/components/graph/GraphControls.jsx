import React from 'react';
import { Panel } from '@xyflow/react';
import { PlayButton } from '../ui/PlayButton.jsx';
import ProgressBar from '../ui/ProgressBar.jsx';
import './GraphControls.css';

export function GraphControls({
                                  handlePlay,
                                  isProcessing,
                                  configLoading
                              }) {
    return (
        <Panel position="bottom-center" className="graph-controls-panel">
            <div className="graph-controls-container">
                <div className="graph-controls-play-wrapper">
                    <PlayButton
                        onClick={handlePlay}
                        isProcessing={isProcessing}
                        disabled={configLoading}
                    />
                </div>
                <div className="graph-controls-progress-wrapper">
                    <ProgressBar />
                </div>
            </div>
        </Panel>
    );
}