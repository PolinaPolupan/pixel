import { useState, useEffect } from 'react';

// Import all node components
import Floor from '../components/graph/nodes/Floor.jsx';
import Input from '../components/graph/nodes/Input.jsx';
import Combine from '../components/graph/nodes/Combine.jsx';
import Output from '../components/graph/nodes/Output.jsx';
import GaussianBlur from '../components/graph/nodes/GaussianBlur.jsx';
import S3Input from '../components/graph/nodes/S3Input.jsx';
import S3Output from '../components/graph/nodes/S3Output.jsx';
import String from '../components/graph/nodes/String.jsx';
import ResNet50 from "../components/graph/nodes/ResNet50.jsx";
import OutputFile from "../components/graph/nodes/OutputFile.jsx";
import MedianBlur from "../components/graph/nodes/MedianBlur.jsx";
import Vector2D from "../components/graph/nodes/Vector2D.jsx";
import Blur from "../components/graph/nodes/Blur.jsx";
import BoxFilter from "../components/graph/nodes/BoxFilter.jsx";
import BilateralFilter from "../components/graph/nodes/BilateralFilter.jsx";

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

export const getParameterColor = (parameterType) => {
    const colorMap = {
        FLOAT: '#ff6b6b', // Reddish
        INT: '#4ecdc4', // Turquoise
        DOUBLE: '#45b7d1', // Blue
        STRING: '#96ceb4', // Greenish
        STRING_ARRAY: '#ffeead', // Yellowish
        FILENAMES_ARRAY: '#d4a5a5', // Pinkish
        VECTOR2D: '#f7a072' // Orange
    };
    return colorMap[parameterType] || '#cccccc'; // Default gray if unknown
};