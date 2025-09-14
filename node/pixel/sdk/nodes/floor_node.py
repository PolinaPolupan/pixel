import math

from pixel.core import Metadata, Node


class FloorNode(Node):
    node_type = "Floor"

    metadata = {
        "inputs": {
            "input": { "type": "DOUBLE", "required": True, "widget": "INPUT", "default": 0.0 }
        },
        "outputs": {
            "output": { "type": "DOUBLE", "required": True, "widget": "LABEL" }
        },
        "display": {
            "category": "Math",
            "description": "Returns the largest integer less than or equal to the input number.",
            "color": "#BA68C8",
            "icon": "FloorIcon"
        }
    }

    def exec(self, input, meta: Metadata = None):
        try:
            number = float(input)
        except (TypeError, ValueError):
            number = 0.0

        return {"output": math.floor(number)}
