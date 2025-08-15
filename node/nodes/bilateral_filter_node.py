from typing import Dict, Any, List

from metadata import Metadata
from node import Node, register_node_class
from storage_client import StorageClient

@register_node_class
class BilateralFilterNode(Node):

    node_type = "BilateralFilter"

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "input": {
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
            "output": {
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

    def exec(self, input: List[str], d: int, sigmaColor: int, sigmaSpace: float, meta: Metadata) -> Dict[str, Any]:
        output_files = []

        for file in input:
            output_files.append(StorageClient.store_from_workspace_to_task(meta.task_id, meta.id, file))


        return {"output": output_files}

    def validate(self, input: List[str], d: int, sigmaColor: int, sigmaSpace: float, meta) -> None:
        pass
