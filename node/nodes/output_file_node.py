from typing import Dict, Any

from node import Node


class OutputFileNode(Node):
    node_type = "OutputFile"

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "content": {
                "type": "STRING",
                "required": False,
                "widget": "INPUT",
                "default": ""
            },
            "filename": {
                "type": "STRING",
                "required": False,
                "widget": "INPUT",
                "default": "new.txt"
            }
        }

    def get_output_types(self) -> Dict[str, Dict[str, Any]]:
        return {}

    def get_display_info(self) -> Dict[str, str]:
        return {
            "category": "IO",
            "description": "Output to a file",
            "color": "#AED581",
            "icon": "OutputIcon"
        }

    def exec(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        content = inputs.get("content", "")
        filename = inputs.get("filename", "new.txt")

        return {}

    def validate(self, inputs: Dict[str, Any]) -> None:
        pass