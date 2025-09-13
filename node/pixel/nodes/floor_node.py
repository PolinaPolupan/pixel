from typing import Dict, Any
import math

from pixel.models import Node


class FloorNode(Node):

    node_type = "Floor"

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "input": {
                "type": "DOUBLE",
                "required": True,
                "widget": "INPUT",
                "default": 0.0
            }
        }

    def get_output_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "output": {
                "type": "DOUBLE",
                "required": True,
                "widget": "LABEL"
            }
        }

    def get_display_info(self) -> Dict[str, str]:
        return {
            "category": "Math",
            "description": "Returns the largest integer less than or equal to the input number.",
            "color": "#BA68C8",
            "icon": "FloorIcon"
        }

    def exec(self, number) -> Dict[str, Any]:
        try:
            number = float(number)
        except (TypeError, ValueError):
            number = 0.0

        return {"output": math.floor(number)}

    def validate(self, number) -> None:
        pass
