import { useReactFlow } from "@xyflow/react";
import { useEffect } from "react";
import NodeHeader from "../ui/NodeHeader.jsx";
import FileUpload from "../file/FileUpload.jsx";
import LabeledHandle from "../handles/LabeledHandle.jsx";
import InputHandle from "../handles/InputHandle.jsx";
import './Node.css';

export default function Node({ id, data }) {
    const { config } = data;
    const reactFlow = useReactFlow();

    if (!config) {
        return (
            <div className="custom-node-error">
                Missing config!
            </div>
        );
    }

    const { inputHandles, outputHandles } = config;

    useEffect(() => {
        const defaultValues = Object.entries(outputHandles || {})
            .filter(([_, h]) => h.default !== undefined)
            .reduce((acc, [handleId, handleConfig]) => {
                if (data[handleId] === undefined) {
                    acc[handleId] = handleConfig.default;
                }
                return acc;
            }, {});

        if (Object.keys(defaultValues).length > 0) {
            reactFlow.updateNodeData(id, defaultValues);
        }
    }, [id, outputHandles, data, reactFlow]);

    const renderInputHandle = (handleId, handleConfig) => {
        if (handleConfig.widget === "LABEL") {
            return (
                <LabeledHandle
                    key={handleId}
                    id={handleId}
                    label={handleId}
                    type="source"
                    position="right"
                    parameterType={handleConfig.source}
                    connectionCount="10"
                />
            );
        }
        return null;
    };

    const renderOutputHandle = (handleId, handleConfig) => {
        if (handleConfig.widget === "LABEL") {
            return (
                <LabeledHandle
                    key={handleId}
                    id={handleId}
                    label={handleId}
                    type="target"
                    position="left"
                    parameterType={handleConfig. target}
                />
            );
        }

        if (handleConfig.widget === "INPUT") {
            return (
                <InputHandle
                    key={handleId}
                    id={id}
                    data={data}
                    handleId={handleId}
                    handleLabel={handleId}
                    parameterType={handleConfig.target}
                    defaultValue={handleConfig.default}
                />
            );
        }

        if (handleConfig.widget === "FILE_PICKER") {
            const handleImagesSelected = (filePaths) => {
                reactFlow. updateNodeData(id, { [handleId]: filePaths });
            };

            return (
                <div key={handleId} className="custom-node-file-picker-wrapper">
                    <FileUpload
                        onFilesSelected={handleImagesSelected}
                        defaultFiles={handleConfig.default}
                    />
                    <LabeledHandle
                        id={handleId}
                        label={handleId}
                        type="target"
                        position="left"
                        connectionCount="10"
                        parameterType={handleConfig.target}
                    />
                </div>
            );
        }

        return null;
    };

    return (
        <div className="custom-node">
            <NodeHeader title={config.display. name} />

            {Object.entries(inputHandles || {}).map(([handleId, handleConfig]) =>
                renderInputHandle(handleId, handleConfig)
            )}

            {Object.entries(outputHandles || {}).map(([handleId, handleConfig]) =>
                renderOutputHandle(handleId, handleConfig)
            )}
        </div>
    );
}