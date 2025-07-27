import NodeHeader from "../ui/NodeHeader.jsx";
import FileUpload from "../file/FileUpload.jsx";
import LabeledHandle from "../handles/LabeledHandle.jsx";
import InputHandle from "../handles/InputHandle.jsx";
import {useReactFlow} from "@xyflow/react";

export default function Node({ id, data }) {
    const { config } = data;
    if (!config) return <div style={{ color: 'red' }}>Missing config!</div>;

    const { component, handles } = config;

    const inputHandles = Object.entries(handles || {})
        .filter(([_, h]) => h.source);
    const outputHandles = Object.entries(handles || {})
        .filter(([_, h]) => h.target);

    return (
        <div style={{minWidth: '100px'}}>
            <NodeHeader title={component} />
            {inputHandles.map(([handleId, handleConfig]) => {
                if (handleConfig.widget === "LABEL") {
                    return (
                        <LabeledHandle
                            id={handleId}
                            label={handleId}
                            type="source"
                            position="right"
                            parameterType={handleConfig.source}
                            connectionCount="10"
                        />
                    );
                }
            })}
            {outputHandles.map(([handleId, handleConfig]) => {
                if (handleConfig.widget === "LABEL") {
                    return (
                        <LabeledHandle
                            label={handleId}
                            type="target"
                            position="left"
                            id={handleId}
                            parameterType={handleConfig.target}
                        />
                    );
                }
                if (handleConfig.widget === "INPUT") {
                    return <InputHandle
                        id={id}
                        data={data}
                        handleId={handleId}
                        handleLabel={handleId}
                        parameterType={handleConfig.target}
                    />
                }
                if (handleConfig.widget === "FILE_PICKER") {
                    const { updateNodeData } = useReactFlow();

                    const handleImagesSelected = (filePaths) => {
                        updateNodeData(id, { files: filePaths }); // Update with relative paths (e.g., output/Picture.jpeg)
                    };

                    return (
                        <div>
                            <FileUpload onFilesSelected={handleImagesSelected}/>
                            <LabeledHandle
                                label={handleId}
                                type="target"
                                position="left"
                                id={handleId}
                                connectionCount="10"
                                parameterType={handleConfig.target}
                            />
                        </div>
                    );
                }
            })}
        </div>
    );
}