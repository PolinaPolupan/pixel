from typing import Dict, Any

from node import Node


class ResNet50Node(Node):
    node_type = "ResNet50"

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "files": {
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

    def exec(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        outputs = {}
        files = inputs.get("files", set())

        if not isinstance(files, set):
            files = set(files) if isinstance(files, (list, tuple)) else set()

        # Implement ResNet50 processing logic here

        return outputs

    def validate(self, inputs: Dict[str, Any]) -> None:
        # Add validation logic if needed
        pass