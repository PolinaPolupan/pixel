from typing import Dict, Any

from pixel.models import Node


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

    def exec(self, content, filename) -> Dict[str, Any]:
        return {}

    def validate(self, content, filename) -> None:
        pass