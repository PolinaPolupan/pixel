import React from 'react';
import { Panel } from '@xyflow/react';
import './GraphControls.css';
import {useProgress} from "../../services/contexts/ProgressContext.jsx";

export function GraphControls({
                                  handlePlay,
                                  isProcessing
                              }) {
    const { progressState } = useProgress();
    return (
        <Panel position="bottom-center" className="graph-controls-panel">
            <div className="graph-controls-container">
                <div className="graph-controls-play-wrapper">
                    <button
                        onClick={handlePlay}
                        disabled={isProcessing}
                        className="play-button"
                    >
                        {isProcessing ?  '⏳' : '▶'}
                    </button>
                </div>
                <div className="graph-controls-progress-wrapper">
                    <div className="progress-bar-container">
                        {progressState.visible ?  (
                            <div className={`progress-bar-wrapper ${progressState.fadeOut ? 'fade-out' : ''}`}>
                                <div
                                    className="progress-bar-fill"
                                    style={{ width: `${progressState.percent}%` }}
                                />
                                <div className="progress-bar-text">
                                    {progressState.current} / {progressState.total} nodes processed ({progressState.percent}%)
                                </div>
                            </div>
                        ) : (
                            <div className="progress-bar-ready">
                                Ready for processing
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </Panel>
    );
}