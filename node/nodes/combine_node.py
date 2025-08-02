from typing import Dict, Any
from node import Node
from storage_client import StorageClient


class CombineNode(Node):

    node_type = "Combine"

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "files_0": {
                "type": "FILEPATH_ARRAY",
                "required": True,
                "widget": "LABEL",
                "default": set()
            },
            "files_1": {
                "type": "FILEPATH_ARRAY",
                "required": False,
                "widget": "LABEL",
                "default": set()
            },
            "files_2": {
                "type": "FILEPATH_ARRAY",
                "required": False,
                "widget": "LABEL",
                "default": set()
            },
            "files_3": {
                "type": "FILEPATH_ARRAY",
                "required": False,
                "widget": "LABEL",
                "default": set()
            },
            "files_4": {
                "type": "FILEPATH_ARRAY",
                "required": False,
                "widget": "LABEL",
                "default": set()
            }
        }

    def get_output_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "files": {
                "type": "FILEPATH_ARRAY",
                "required": True,
                "widget": "LABEL"
            }
        }

    def get_display_info(self) -> Dict[str, str]:
        return {
            "category": "IO",
            "description": "Combine multiple data sources into a single source",
            "color": "#AED581",
            "icon": "CombineIcon"
        }

    def exec(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        files = set()

        for i in range(5):
            key = f"files_{i}"
            if key in inputs and inputs[key] is not None:
                file_set = inputs[key]
                if not isinstance(file_set, set):
                    file_set = set(file_set) if isinstance(file_set, (list, tuple)) else set()
                files.update(file_set)

        task_id = inputs.get("meta", {}).get("taskId")
        node_id = inputs.get("meta", {}).get("nodeId")

        output_files = []

        for file in files:
            output_files.append(StorageClient.store_from_workspace_to_task(task_id, node_id, file))

        return {"files": output_files}

    def validate(self, inputs: Dict[str, Any]) -> None:
        pass
