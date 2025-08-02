import os
from typing import Dict, Any

from node import Node


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

    def exec(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        files = inputs.get("files", set())
        prefix = inputs.get("prefix", "")
        folder = inputs.get("folder", "")

        if not isinstance(files, set):
            files = set(files) if isinstance(files, (list, tuple)) else set()

        scene_id = inputs.get("meta", {}).get("sceneId")

        if scene_id:
            for filepath in files:
                from storage_client import StorageClient
                StorageClient.store_from_workspace_to_scene(
                    scene_id=scene_id,
                    source=filepath,
                    folder=folder if folder else None,
                    prefix=prefix if prefix else None
                )

        return {}

    def validate(self, inputs: Dict[str, Any]) -> None:

        files = inputs.get("files", set())

        if not isinstance(files, set):
            files = set(files) if isinstance(files, (list, tuple)) else set()

        for filepath in files:
            if not os.path.exists(filepath):
                raise ValueError(f"File does not exist: {filepath}")