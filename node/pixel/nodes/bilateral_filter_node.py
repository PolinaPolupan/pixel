from typing import List

from pixel import StorageClient
from pixel.models import Node, Metadata


class BilateralFilterNode(Node):

    node_type = "BilateralFilter"

    def get_input_types(self):
        return {
            "input": {
                "type": "FILEPATH_ARRAY",
                "required": True,
                "widget": "LABEL",
                "default": set()
            },
            "d": {
                "type": "INT",
                "required": True,
                "widget": "INPUT",
                "default": 9
            },
            "sigmaColor": {
                "type": "DOUBLE",
                "required": True,
                "widget": "INPUT",
                "default": 75.0
            },
            "sigmaSpace": {
                "type": "DOUBLE",
                "required": True,
                "widget": "INPUT",
                "default": 75.0
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
            "description": "Applies a bilateral filter to the input image.",
            "color": "#FF8A65",
            "icon": "BlurIcon"
        }

    def exec(self, input: List[str], d: int, sigmaColor: int, sigmaSpace: float, meta: Metadata):
        output_files = []

        for file in input:
            output_files.append(StorageClient.store_from_workspace_to_task(meta.task_id, meta.id, file))


        return {"output": output_files}

    def validate(self, input: List[str], d: int, sigmaColor: int, sigmaSpace: float, meta):
        pass
