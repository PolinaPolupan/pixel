from pixel.core import Metadata, Node
from pixel.sdk import StorageClient


class MedianBlurNode(Node):
    node_type = "MedianBlur"

    metadata = {
        "inputs": {
            "input": { "type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL", "default": set() },
            "ksize": { "type": "INT", "required": True, "widget": "INPUT", "default": 3 }
        },
        "outputs": {
            "output": { "type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL" }
        },
        "display": {
            "category": "Filtering",
            "description": "Blurs an image using the specified kernel size",
            "color": "#FF8A65",
            "icon": "BlurIcon"
        }
    }

    def exec(self, input, ksize, meta: Metadata):
        output_files = []
        for file in input:
            output_files.append(StorageClient.store_from_workspace_to_task(meta.task_id, meta.id, file))
        return {"output": output_files}

    def validate(self, input, ksize, meta: Metadata):
        try:
            ksize = int(ksize)
        except (TypeError, ValueError):
            raise ValueError("ksize must be an integer")
        if ksize < 2 or ksize % 2 == 0:
            raise ValueError("KSize must be greater than 1 and odd")
