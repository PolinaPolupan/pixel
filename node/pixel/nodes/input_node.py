from typing import Dict, Any

from pixel import StorageClient
from pixel.models import Metadata, Node


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

    def exec(self, input, meta: Metadata) -> Dict[str, Any]:
        output_files = []

        for file in input:
            output_files.append(StorageClient.store_from_workspace_to_task(meta.task_id, meta.id, file))

        return {"output": output_files}

    def validate(self, input, meta: Metadata) -> None:
        pass
