from typing import Dict, Any

from pixel import StorageClient
from pixel.models import Node, Metadata


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
            "output": {
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

    def exec(self, meta: Metadata, files_0=None, files_1=None, files_2=None, files_3=None, files_4=None) -> Dict[str, Any]:
        files = set()

        file_params = [files_0, files_1, files_2, files_3, files_4]
        for file_set in file_params:
            if file_set is not None:
                if not isinstance(file_set, set):
                    file_set = set(file_set) if isinstance(file_set, (list, tuple)) else {file_set}
                files.update(file_set)

        output_files = []

        for file in files:
            output_files.append(StorageClient.store_from_workspace_to_task(meta.task_id, meta.id, file))

        return {"output": output_files}

    def validate(self, meta: Metadata, files_0=None, files_1=None, files_2=None, files_3=None, files_4=None) -> None:
        pass
