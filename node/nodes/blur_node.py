from typing import Dict, Any
from node import Node


class BlurNode(Node):

    node_type = "Blur"

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "files": {
                "type": "FILEPATH_ARRAY",
                "required": True,
                "widget": "LABEL",
                "default": set()
            },
            "ksize": {
                "type": "VECTOR2D",
                "required": True,
                "widget": "LABEL",
                "default": {"x": 3, "y": 3}
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

    def exec(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        files = inputs.get("files", set())
        ksize = inputs.get("ksize")

        if not isinstance(files, set):
            files = set(files) if isinstance(files, (list, tuple)) else set()


        return {"files": files}

    def validate(self, inputs: Dict[str, Any]) -> None:
        ksize = inputs.get("ksize")

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
