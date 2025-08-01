from typing import Dict, Any
from node import Node


class StringNode(Node):

    node_type = "String"

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "input": {
                "type": "STRING",
                "required": True,
                "widget": "INPUT",
                "default": ""
            }
        }

    def get_output_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "output": {
                "type": "STRING",
                "required": True
            }
        }

    def get_display_info(self) -> Dict[str, str]:
        return {
            "category": "Types",
            "description": "String",
            "color": "#AED581",
            "icon": "StringIcon"
        }

    def exec(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        value = inputs.get("input", "")

        if not isinstance(value, str):
            value = str(value)

        return {"output": value}

    def validate(self, inputs: Dict[str, Any]) -> None:
        pass