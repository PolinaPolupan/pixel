import os
from typing import Dict, Any

from metadata import Metadata
from node import Node, register_node_class


@register_node_class
class OutputNode(Node):

    node_type = "Output"

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "files": {
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

    def exec(self, files, prefix, folder, meta: Metadata) -> Dict[str, Any]:
        for filepath in files:
            from storage_client import StorageClient
            StorageClient.store_from_workspace_to_scene(
                scene_id=meta.scene_id,
                source=filepath,
                folder=folder if folder else None,
                prefix=prefix if prefix else None
            )

        return {}

    def validate(self, files, prefix, folder, meta) -> None:
        pass