from typing import Dict, Any
from node import Node


class InputNode(Node):

    node_type = "Input"

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "input": {
                "type": "FILEPATH_ARRAY",
                "required": True,
                "widget": "FILE_PICKER",
                "default": set()
            }
        }

    def get_output_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "output": {
                "type": "FILEPATH_ARRAY",
                "required": True,
                "widget": "LABEL"
            }
        }

    def get_display_info(self) -> Dict[str, str]:
        return {
            "category": "IO",
            "description": "Input files",
            "color": "#AED581",
            "icon": "InputIcon"
        }

    def exec(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        files = inputs.get("input", set())

        if not isinstance(files, set):
            files = set(files) if isinstance(files, (list, tuple)) else set()

        return {"output": files}

    def validate(self, inputs: Dict[str, Any]) -> None:
        pass
