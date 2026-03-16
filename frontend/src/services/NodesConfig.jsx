import { useState, useEffect } from 'react';
import { IoFolderOutline, IoSaveOutline, IoReload, IoArrowDown, IoCloudOutline } from 'react-icons/io5';
import { nodeApi } from "./api.js";

const iconComponents = {
    InputIcon: () => (
        <div style={{ width: '16px', height: '16px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <IoFolderOutline style={{ fontSize: '16px', color: 'var(--icon-color)' }} />
        </div>
    ),
    OutputIcon: () => (
        <div style={{ width: '16px', height: '16px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <IoSaveOutline style={{ fontSize: '16px', color: 'var(--icon-color)' }} />
        </div>
    ),
    CombineIcon: () => (
        <div style={{ width: '16px', height: '16px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <IoReload style={{ fontSize: '16px', color: 'var(--icon-color)' }} />
        </div>
    ),
    FloorIcon: () => (
        <div style={{ width: '16px', height: '16px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <IoArrowDown style={{ fontSize: '16px', color: 'var(--icon-color)' }} />
        </div>
    ),
    CloudIcon: () => (
        <div style={{ width: '16px', height: '16px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <IoCloudOutline style={{ fontSize: '16px', color: 'var(--icon-color)' }} />
        </div>
    ),
    BlurIcon: () => (
        <div style={{
            width: '16px', height: '16px', borderRadius: '50%',
            background: 'radial-gradient(circle, rgba(255,255,255,0.15), rgba(255,255,255,0))',
            filter: 'blur(2px)', border: `1px solid var(--icon-color)`
        }} />
    ),
    StringBadge: () => (
        <div style={{
            width: '16px', height: '16px', borderRadius: '4px', background: 'var(--color-accent, #5a6a7a)',
            color: 'var(--color-bg-primary, #fff)', display: 'flex', alignItems: 'center',
            justifyContent: 'center', fontSize: '10px', fontWeight: 'bold'
        }}>S</div>
    ),
    DefaultIcon: () => (
        <div style={{
            width: '16px', height: '16px', borderRadius: '4px',
            background: 'var(--color-bg-secondary, #2c343a)',
            border: '1px solid var(--icon-color)'
        }} />
    )
};

export function useNodesConfig() {
    const [nodesConfig, setNodesConfig] = useState({});
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchNodesConfig = async () => {
            try {
                setIsLoading(true);
                setError(null);

                const data = await nodeApi.getNodeConfig();

                const processedConfig = {};

                Object.entries(data).forEach(([nodeType, config]) => {
                    const IconComponent = iconComponents[config.display?.icon] || iconComponents.DefaultIcon;

                    const inputHandles = {};
                    const outputHandles = {};

                    Object.entries(config.inputs || {}).forEach(([inputId, inputConfig]) => {
                        outputHandles[inputId] = {
                            ...inputConfig,
                            target: inputConfig.type,
                        };
                    });

                    Object.entries(config.outputs || {}).forEach(([outputId, outputConfig]) => {
                        inputHandles[outputId] = {
                            ...outputConfig,
                            source: outputConfig.type,
                            widget: outputConfig.widget || "LABEL"
                        };
                    });

                    processedConfig[nodeType] = {
                        nodeType: nodeType,
                        handles: {
                            ...inputHandles,
                            ...outputHandles
                        },
                        inputHandles,
                        outputHandles,
                        display: {
                            ...config.display,
                            icon: IconComponent
                        }
                    };
                });

                setNodesConfig(processedConfig);
            } catch (err) {
                console.error('Error fetching node configurations:', err);
                setError(err.message);
            } finally {
                setIsLoading(false);
            }
        };

        fetchNodesConfig();
    }, []);

    return { nodesConfig, isLoading, error };
}

export const getParameterColor = (parameterType) => {
    const colorMap = {
        FLOAT: '#ff6b6b',
        INT: '#4ecdc4',
        DOUBLE: '#45b7d1',
        STRING: '#96ceb4',
        STRING_ARRAY: '#ffeead',
        FILEPATH_ARRAY: '#d4a5a5',
        VECTOR2D: '#f7a072'
    };
    return colorMap[parameterType] || '#cccccc';
};