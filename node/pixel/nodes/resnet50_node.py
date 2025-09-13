from typing import Dict, Any

from pixel.models import Node


class ResNet50Node(Node):
    node_type = "ResNet50"

    required_packages = ["numpy==2.3.2", "pillow==11.3.0"]

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "input": {
                "type": "FILEPATH_ARRAY",
                "required": True,
                "widget": "LABEL",
                "default": set()
            }
        }

    def get_output_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "json": {
                "type": "STRING",
                "required": True
            }
        }

    def get_display_info(self) -> Dict[str, str]:
        return {
            "category": "ML",
            "description": "Run ResNet50 on images",
            "color": "#81C784",
            "icon": "ResNet50Icon"
        }

    def exec(self, input) -> Dict[str, Any]:
        outputs = {}
        return outputs

    def validate(self, input) -> None:
        pass