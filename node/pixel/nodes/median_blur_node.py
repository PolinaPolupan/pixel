from typing import Dict, Any

from pixel import StorageClient
from pixel.models import Metadata, Node


class MedianBlurNode(Node):

    node_type = "MedianBlur"

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "input": {
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
            "output": {
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

    def exec(self, input, ksize, meta: Metadata) -> Dict[str, Any]:
        output_files = []

        for file in input:
            output_files.append(StorageClient.store_from_workspace_to_task(meta.task_id, meta.id, file))


        return {"output": output_files}

    def validate(self, input, ksize, meta) -> None:
        try:
            ksize = int(ksize)
        except (TypeError, ValueError):
            raise ValueError("ksize must be an integer")

        if ksize < 2 or ksize % 2 == 0:
            raise ValueError("KSize must be greater than 1 and odd")
