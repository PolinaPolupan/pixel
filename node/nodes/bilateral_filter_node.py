from typing import Dict, Any
from node import Node
from storage_client import StorageClient


class BilateralFilterNode(Node):

    node_type = "BilateralFilter"

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "files": {
                "type": "FILEPATH_ARRAY",
                "required": True,
                "widget": "LABEL",
                "default": set()
            },
            "d": {
                "type": "INT",
                "required": True,
                "widget": "INPUT",
                "default": 9
            },
            "sigmaColor": {
                "type": "DOUBLE",
                "required": True,
                "widget": "INPUT",
                "default": 75.0
            },
            "sigmaSpace": {
                "type": "DOUBLE",
                "required": True,
                "widget": "INPUT",
                "default": 75.0
            }
        }

    def get_output_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "files": {
                "type": "FILEPATH_ARRAY",
                "required": True,
                "widget": "LABEL"
            }
        }

    def get_display_info(self) -> Dict[str, str]:
        return {
            "category": "Filtering",
            "description": "Applies a bilateral filter to the input image.",
            "color": "#FF8A65",
            "icon": "BlurIcon"
        }

    def exec(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        files = inputs.get("files", set())
        sigmaColor = inputs.get("sigmaColor")
        sigmaSpace = inputs.get("sigmaSpace")
        d = inputs.get("d")

        if not isinstance(files, set):
            files = set(files) if isinstance(files, (list, tuple)) else set()

        task_id = inputs.get("meta", {}).get("taskId")
        node_id = inputs.get("meta", {}).get("nodeId")

        output_files = []

        for file in files:
            output_files.append(StorageClient.store_from_workspace_to_task(task_id, node_id, file))


        return {"files": output_files}

    def validate(self, inputs: Dict[str, Any]) -> None:
        pass
