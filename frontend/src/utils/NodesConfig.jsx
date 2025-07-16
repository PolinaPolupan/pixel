import { useState, useEffect } from 'react';

// Import all node components
import Floor from '../components/nodes/Floor';
import Input from '../components/nodes/Input';
import Combine from '../components/nodes/Combine';
import Output from '../components/nodes/Output';
import GaussianBlur from '../components/nodes/GaussianBlur';
import S3Input from '../components/nodes/S3Input';
import S3Output from '../components/nodes/S3Output';
import String from '../components/nodes/String';
import ResNet50 from "../components/nodes/ResNet50";
import OutputFile from "../components/nodes/OutputFile";
import MedianBlur from "../components/nodes/MedianBlur";
import Vector2D from "../components/nodes/Vector2D";
import Blur from "../components/nodes/Blur";
import BoxFilter from "../components/nodes/BoxFilter";
import BilateralFilter from "../components/nodes/BilateralFilter";

// Import icons
import { IoFolderOutline, IoSaveOutline, IoReload, IoArrowDown, IoCloudOutline } from 'react-icons/io5';
import {nodeApi} from "./api.js";

// Define component mapping
const componentMap = {
    Floor,
    Input,
    Combine,
    Output,
    GaussianBlur,
    S3Input,
    S3Output,
    String,
    ResNet50,
    OutputFile,
    MedianBlur,
    Vector2D,
    Blur,
    BoxFilter,
    BilateralFilter
};

// Define icon components
const iconComponents = {
    InputIcon: ({ theme }) => (
        <div style={{ width: '16px', height: '16px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <IoFolderOutline style={{ fontSize: '16px', color: theme?.colors?.onSurface || '#d8dde2' }} />
        </div>
    ),
    OutputIcon: ({ theme }) => (
        <div style={{ width: '16px', height: '16px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <IoSaveOutline style={{ fontSize: '16px', color: theme?.colors?.onSurface || '#d8dde2' }} />
        </div>
    ),
    BlurIcon: ({ theme }) => (
        <div style={{
            width: '16px', height: '16px', borderRadius: '50%',
            background: 'radial-gradient(circle, rgba(255,255,255,0.3), rgba(255,255,255,0))',
            filter: 'blur(2px)', border: `1px solid ${theme?.colors?.onSurface || '#d8dde2'}`
        }} />
    ),
    CombineIcon: ({ theme }) => (
        <div style={{ width: '16px', height: '16px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <IoReload style={{ fontSize: '16px', color: theme?.colors?.onSurface || '#d8dde2' }} />
        </div>
    ),
    FloorIcon: ({ theme }) => (
        <div style={{ width: '16px', height: '16px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <IoArrowDown style={{ fontSize: '16px', color: theme?.colors?.onSurface || '#d8dde2' }} />
        </div>
    ),
    CloudIcon: ({ theme }) => (
        <div style={{ width: '16px', height: '16px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <IoCloudOutline style={{ fontSize: '16px', color: theme?.colors?.onSurface || '#d8dde2' }} />
        </div>
    ),
    StringBadge: ({ theme }) => (
        <div style={{
            width: '16px', height: '16px', borderRadius: '4px', background: theme?.colors?.primary || '#5a6a7a',
            color: theme?.colors?.onPrimary || '#d8dde2', display: 'flex', alignItems: 'center',
            justifyContent: 'center', fontSize: '10px', fontWeight: 'bold'
        }}>S</div>
    ),
    DefaultIcon: ({ theme }) => (
        <div style={{
            width: '16px', height: '16px', borderRadius: '4px',
            background: theme?.colors?.surface || '#2c343a',
            border: `1px solid ${theme?.colors?.onSurface || '#d8dde2'}`
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

                // Use the centralized API client
                const data = await nodeApi.getNodeConfig();

                const processedConfig = {};

                Object.entries(data).forEach(([nodeType, config]) => {
                    const component = componentMap[nodeType];

                    if (!component) {
                        console.warn(`No component found for node type: ${nodeType}`);
                    }

                    const iconName = config.display.icon;
                    const IconComponent = iconComponents[iconName] || iconComponents.DefaultIcon;

                    processedConfig[nodeType] = {
                        ...config,
                        component,
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

// For legacy support, also export a static config
export const nodesConfig = {}; // This will be replaced by the fetched config