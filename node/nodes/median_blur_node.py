from typing import Dict, Any

from metadata import Metadata
from node import Node
from storage_client import StorageClient


class MedianBlurNode(Node):

    node_type = "MedianBlur"

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "files": {
                "type": "FILEPATH_ARRAY",
                "required": True,
                "widget": "LABEL",
                "default": set()
            },
            "ksize": {
                "type": "INT",
                "required": True,
                "widget": "INPUT",
                "default": 3
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

    def exec(self, files, ksize, meta: Metadata) -> Dict[str, Any]:
        output_files = []

        for file in files:
            output_files.append(StorageClient.store_from_workspace_to_task(meta.task_id, meta.id, file))


        return {"files": output_files}

    def validate(self, files, ksize, meta) -> None:
        try:
            ksize = int(ksize)
        except (TypeError, ValueError):
            raise ValueError("ksize must be an integer")

        if ksize < 2 or ksize % 2 == 0:
            raise ValueError("KSize must be greater than 1 and odd")
