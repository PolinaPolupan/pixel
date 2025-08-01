from typing import Dict, Any
from node import Node


class GaussianBlurNode(Node):

    node_type = "GaussianBlur"

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "files": {
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
            "files": {
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

    def exec(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        files = inputs.get("files", set())
        sizeX = inputs.get("sizeX")
        sizeY = inputs.get("sizeY", sizeX)
        sigmaX = inputs.get("sigmaX", 0.0)
        sigmaY = inputs.get("sigmaY", 0.0)

        if not isinstance(files, set):
            files = set(files) if isinstance(files, (list, tuple)) else set()


        return {"files": files}

    def validate(self, inputs: Dict[str, Any]) -> None:
        sizeX = inputs.get("sizeX")
        sizeY = inputs.get("sizeY", sizeX)
        sigmaX = inputs.get("sigmaX", 0.0)
        sigmaY = inputs.get("sigmaY", 0.0)

        try:
            sizeX = int(sizeX)
            sizeY = int(sizeY)
            sigmaX = float(sigmaX)
            sigmaY = float(sigmaY)
        except (TypeError, ValueError):
            raise ValueError("Invalid parameter types")

        if sizeX < 0 or sizeX % 2 == 0:
            raise ValueError("SizeX must be positive and odd")
        if sizeY < 0 or sizeY % 2 == 0:
            raise ValueError("SizeY must be positive and odd")
        if sigmaX < 0:
            raise ValueError("SigmaX must be positive")
        if sigmaY < 0:
            raise ValueError("SigmaY must be positive")
