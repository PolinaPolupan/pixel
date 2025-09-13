from typing import List

from pixel import StorageClient
from pixel.models import Node, Metadata


class BlurNode(Node):

    node_type = "Blur"

    def get_input_types(self):
        return {
            "input": {
                "type": "FILEPATH_ARRAY",
                "required": True,
                "widget": "LABEL",
                "default": set()
            },
            "ksize": {
                "type": "VECTOR2D",
                "required": True,
                "widget": "LABEL",
                "default": {"x": 3, "y": 3}
            }
        }

    def get_output_types(self):
        return {
            "output": {
                "type": "FILEPATH_ARRAY",
                "required": True,
                "widget": "LABEL"
            }
        }

    def get_display_info(self):
        return {
            "category": "Filtering",
            "description": "Blurs an image using the specified kernel size",
            "color": "#FF8A65",
            "icon": "BlurIcon"
        }

    def exec(self, input: List[str], ksize, meta: Metadata):
        output_files = []

        for file in input:
            output_files.append(StorageClient.store_from_workspace_to_task(meta.task_id, meta.id, file))

        return {"output": output_files}

    def validate(self, input: List[str], ksize, meta: Metadata):
        if isinstance(ksize, dict):
            x = ksize.get("x", 0)
            y = ksize.get("y", 0)
        else:
            x = ksize.x
            y = ksize.y

        try:
            x = int(x)
            y = int(y)
        except (TypeError, ValueError):
            raise ValueError("KSize values must be convertible to integers")

        if x < 1 or y < 1:
            raise ValueError("KSize must be greater than 0")
