from pixel.core import Node, Metadata
from pixel.sdk import StorageClient


class CombineNode(Node):
    node_type = "Combine"

    metadata = {
        "inputs": {
            "files_0": { "type": "FILEPATH_ARRAY", "required": True,  "widget": "LABEL", "default": set() },
            "files_1": { "type": "FILEPATH_ARRAY", "required": False, "widget": "LABEL", "default": set() },
            "files_2": { "type": "FILEPATH_ARRAY", "required": False, "widget": "LABEL", "default": set() },
            "files_3": { "type": "FILEPATH_ARRAY", "required": False, "widget": "LABEL", "default": set() },
            "files_4": { "type": "FILEPATH_ARRAY", "required": False, "widget": "LABEL", "default": set() }
        },
        "outputs": {
            "output": { "type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL" }
        },
        "display": {
            "category": "IO",
            "description": "Combine multiple data sources into a single source",
            "color": "#AED581",
            "icon": "CombineIcon"
        }
    }

    def exec(self, meta: Metadata, files_0=None, files_1=None, files_2=None, files_3=None, files_4=None):
        files = set()
        file_params = [files_0, files_1, files_2, files_3, files_4]

        for file_set in file_params:
            if file_set is not None:
                if not isinstance(file_set, set):
                    file_set = set(file_set) if isinstance(file_set, (list, tuple)) else {file_set}
                files.update(file_set)

        output_files = [
            StorageClient.store_from_workspace_to_task(meta.task_id, meta.id, file)
            for file in files
        ]

        return {"output": output_files}
