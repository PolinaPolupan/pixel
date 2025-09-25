import {useReactFlow} from "@xyflow/react";
import {useNodesApi} from "./useNodesApi.js";
import {useCallback} from "react";

export function useCreateNode() {
    const { getNodes, addNodes } = useReactFlow();
    const { getDefaultInputs, nodesConfig } = useNodesApi();

    const createNode = useCallback(
        (type, position) => {
            const nodeIds = getNodes().map((n) => parseInt(n.id));
            const newId = (Math.max(...nodeIds, 0) + 1).toString();

            addNodes({
                id: newId,
                type,
                position,
                data: {
                    ...getDefaultInputs(type),
                    config: nodesConfig[type],
                },
            });
        },
        [getNodes, addNodes, getDefaultInputs, nodesConfig]
    );

    return { createNode };
}