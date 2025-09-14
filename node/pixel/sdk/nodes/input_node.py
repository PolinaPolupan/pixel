from pixel.core import Metadata, Node
from pixel.sdk import StorageClient


class InputNode(Node):
    node_type = "Input"

    metadata = {
        "inputs": {
            "input": { "type": "FILEPATH_ARRAY", "required": True, "widget": "FILE_PICKER", "default": set() }
        },
        "outputs": {
            "output": { "type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL" }
        },
        "display": {
            "category": "IO",
            "description": "Input files",
            "color": "#AED581",
            "icon": "InputIcon"
        }
    }

    def exec(self, input, meta: Metadata):
        output_files = []
        for file in input:
            output_files.append(StorageClient.store_from_workspace_to_task(meta.task_id, meta.id, file))
        return {"output": output_files}
