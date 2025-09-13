import os
from typing import Dict, Any, List

from pixel import StorageClient
from pixel.models import Metadata, Node


class OutputNode(Node):

    node_type = "Output"

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "input": {
                "type": "FILEPATH_ARRAY",
                "required": True,
                "widget": "LABEL",
                "default": set()
            },
            "prefix": {
                "type": "STRING",
                "required": False,
                "widget": "INPUT",
                "default": ""
            },
            "folder": {
                "type": "STRING",
                "required": False,
                "widget": "INPUT",
                "default": ""
            }
        }

    def get_output_types(self) -> Dict[str, Dict[str, Any]]:
        return {}

    def get_display_info(self) -> Dict[str, str]:
        return {
            "category": "IO",
            "description": "Output files to a folder",
            "color": "#AED581",
            "icon": "OutputIcon"
        }

    def exec(self, input: List[str], prefix, folder, meta: Metadata) -> Dict[str, Any]:
        for filepath in input:
            StorageClient.store_from_workspace_to_scene(
                scene_id=meta.scene_id,
                source=filepath,
                folder=folder if folder else None,
                prefix=prefix if prefix else None
            )

        return {}

    def validate(self, input: List[str], prefix, folder, meta) -> None:
        pass