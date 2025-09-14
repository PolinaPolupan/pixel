from typing import List

from pixel.core import Metadata, Node
from pixel.sdk import StorageClient


class OutputNode(Node):
    node_type = "Output"

    metadata = {
        "inputs": {
            "input": { "type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL", "default": set() },
            "prefix": { "type": "STRING", "required": False, "widget": "INPUT", "default": "" },
            "folder": { "type": "STRING", "required": False, "widget": "INPUT", "default": "" }
        },
        "outputs": {},
        "display": {
            "category": "IO",
            "description": "Output files to a folder",
            "color": "#AED581",
            "icon": "OutputIcon"
        }
    }

    def exec(self, input: List[str], prefix, folder, meta: Metadata):
        for filepath in input:
            StorageClient.store_from_workspace_to_scene(
                scene_id=meta.scene_id,
                source=filepath,
                folder=folder if folder else None,
                prefix=prefix if prefix else None
            )
        return {}
