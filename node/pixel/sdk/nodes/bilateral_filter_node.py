from typing import List

from pixel.core import Node, Metadata
from pixel.sdk import StorageClient


class BilateralFilterNode(Node):

    node_type = "BilateralFilter"

    metadata = {
        "inputs": {
            "input": { "type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL", "default": set() },
            "d": { "type": "INT", "required": True, "widget": "INPUT", "default": 9 },
            "sigmaColor": { "type": "DOUBLE", "required": True, "widget": "INPUT", "default": 75.0 },
            "sigmaSpace": { "type": "DOUBLE", "required": True, "widget": "INPUT", "default": 75.0 }
        },
        "outputs": {
            "output": { "type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL" }
        },
        "display": {
            "category": "Filtering",
            "description": "Applies a bilateral filter to the input image.",
            "color": "#FF8A65",
            "icon": "BlurIcon"
        }
    }

    def exec(self, input: List[str], d: int, sigmaColor: int, sigmaSpace: float, meta: Metadata):
        output_files = []

        for file in input:
            output_files.append(StorageClient.store_from_workspace_to_task(meta.task_id, meta.id, file))


        return {"output": output_files}
