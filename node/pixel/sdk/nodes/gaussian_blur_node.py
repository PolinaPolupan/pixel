from typing import List

from pixel.core import Node, Metadata
from pixel.sdk import StorageClient


class GaussianBlurNode(Node):
    node_type = "GaussianBlur"

    metadata = {
        "inputs": {
            "input": { "type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL", "default": set() },
            "sizeX": { "type": "INT", "required": True, "widget": "INPUT", "default": 3 },
            "sizeY": { "type": "INT", "required": False, "widget": "INPUT", "default": 3 },
            "sigmaX": { "type": "DOUBLE", "required": False, "widget": "INPUT", "default": 0.0 },
            "sigmaY": { "type": "DOUBLE", "required": False, "widget": "INPUT", "default": 0.0 }
        },
        "outputs": {
            "output": { "type": "FILEPATH_ARRAY", "required": True }
        },
        "display": {
            "category": "Filtering",
            "description": "Blurs an image using a Gaussian kernel",
            "color": "#FF8A65",
            "icon": "BlurIcon"
        }
    }

    def exec(self, input: List[str], sizeX, sizeY, sigmaX, meta: Metadata, sigmaY=0):
        output_files = []
        for file in input:
            output_files.append(StorageClient.store_from_workspace_to_task(meta.task_id, meta.id, file))
        return {"output": output_files}

    def validate(self, input: List[str], sizeX, sizeY, sigmaX, meta: Metadata, sigmaY=0):
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
