from numbers import Number

from pixel.core import Node
from pixel.sdk.models import Vector2D


class Vector2DNode(Node):
    node_type = "Vector2D"

    metadata = {
        "inputs": {
            "x": { "type": "DOUBLE", "required": True, "widget": "INPUT", "default": 0.0 },
            "y": { "type": "DOUBLE", "required": True, "widget": "INPUT", "default": 0.0 }
        },
        "outputs": {
            "vector2D": { "type": "VECTOR2D", "required": True }
        },
        "display": {
            "category": "Types",
            "description": "Creates a 2D vector",
            "color": "#FF8A65",
            "icon": "Vector2DIcon"
        }
    }

    def exec(self, x: Number = 0, y: Number = 0):
        vector2d = Vector2D(x, y)
        return {"vector2D": vector2d.to_dict()}
