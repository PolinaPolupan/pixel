from typing import Dict, Any, List

from metadata import Metadata
from node import Node, register_node_class
from storage_client import StorageClient

@register_node_class
class BoxFilterNode(Node):

    node_type = "BoxFilter"

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "files": {
                "type": "FILEPATH_ARRAY",
                "required": True,
                "widget": "LABEL",
                "default": set()
            },
            "ddepth": {
                "type": "INT",
                "required": True,
                "widget": "INPUT",
                "default": 0
            },
            "ksize": {
                "type": "VECTOR2D",
                "required": True,
                "widget": "LABEL",
                "default": {"x": 1, "y": 1}
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
            "description": "Blurs an image using the specified kernel size",
            "color": "#FF8A65",
            "icon": "BlurIcon"
        }

    def exec(self, files: List[str], ksize, ddepth, meta: Metadata) -> Dict[str, Any]:
        output_files = []

        for file in files:
            output_files.append(StorageClient.store_from_workspace_to_task(meta.task_id, meta.id, file))

        return {"files": output_files}

    def validate(self, files: List[str], ksize, ddepth, meta: Metadata) -> None:
        if isinstance(ksize, dict):
            x = ksize.get("x", 0)
            y = ksize.get("y", 0)
        else:
            x = ksize.x
            y = ksize.y

        try:
            x = int(x)
            y = int(y)
        except (TypeError, ValueError):
            raise ValueError("KSize values must be convertible to integers")

        if x < 1 or y < 1:
            raise ValueError("KSize must be greater than 0")
