from typing import Dict, Any, List

from pixel import StorageClient
from pixel.models import Node, Metadata


class GaussianBlurNode(Node):

    node_type = "GaussianBlur"

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "input": {
                "type": "FILEPATH_ARRAY",
                "required": True,
                "widget": "LABEL",
                "default": set()
            },
            "sizeX": {
                "type": "INT",
                "required": True,
                "widget": "INPUT",
                "default": 3
            },
            "sizeY": {
                "type": "INT",
                "required": False,
                "widget": "INPUT",
                "default": 3
            },
            "sigmaX": {
                "type": "DOUBLE",
                "required": False,
                "widget": "INPUT",
                "default": 0.0
            },
            "sigmaY": {
                "type": "DOUBLE",
                "required": False,
                "widget": "INPUT",
                "default": 0.0
            }
        }

    def get_output_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "output": {
                "type": "FILEPATH_ARRAY",
                "required": True
            }
        }

    def get_display_info(self) -> Dict[str, str]:
        return {
            "category": "Filtering",
            "description": "Blurs an image using a Gaussian kernel",
            "color": "#FF8A65",
            "icon": "BlurIcon"
        }

    def exec(self, input: List[str], sizeX, sizeY, sigmaX, meta: Metadata, sigmaY=0) -> Dict[str, Any]:
        output_files = []

        for file in input:
            output_files.append(StorageClient.store_from_workspace_to_task(meta.task_id, meta.id, file))

        return {"output": output_files}

    def validate(self, input: List[str], sizeX, sizeY, sigmaX, meta: Metadata, sigmaY=0) -> None:
        try:
            sizeX = int(sizeX)
            sizeY = int(sizeY)
            sigmaX = float(sigmaX)
            sigmaY = float(sigmaY)
        except (TypeError, ValueError):
            raise ValueError("Invalid parameter models")

        if sizeX < 0 or sizeX % 2 == 0:
            raise ValueError("SizeX must be positive and odd")
        if sizeY < 0 or sizeY % 2 == 0:
            raise ValueError("SizeY must be positive and odd")
        if sigmaX < 0:
            raise ValueError("SigmaX must be positive")
        if sigmaY < 0:
            raise ValueError("SigmaY must be positive")
