from numbers import Number
from typing import Dict, Any

from pixel.models import Node, Vector2D


class Vector2DNode(Node):

    node_type = "Vector2D"

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "x": {
                "type": "DOUBLE",
                "required": True,
                "widget": "INPUT",
                "default": 0.0
            },
            "y": {
                "type": "DOUBLE",
                "required": True,
                "widget": "INPUT",
                "default": 0.0
            }
        }

    def get_output_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "vector2D": {
                "type": "VECTOR2D",
                "required": True
            }
        }

    def get_display_info(self) -> Dict[str, str]:
        return {
            "category": "Types",
            "description": "Creates a 2D vector",
            "color": "#FF8A65",
            "icon": "Vector2DIcon"
        }

    def exec(self, x: Number=0, y: Number=0) -> Dict[str, Any]:
        vector2d = Vector2D(x, y)

        return {"vector2D": vector2d.to_dict()}

    def validate(self, x: Number=0, y: Number=0) -> None:
        pass
