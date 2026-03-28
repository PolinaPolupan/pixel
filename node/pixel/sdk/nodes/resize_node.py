from typing import List
from pixel.core import Metadata
from pixel.sdk import Client
from pixel.sdk.models.node_decorator import node
import cv2


def resize_exec(input: List[str], width: int, height: int, meta: Metadata):
    output_files = []

    width = int(width)
    height = int(height)

    for file in input:
        img = Client.load_image(file)

        resized = cv2.resize(img, (width, height))

        out_path = Client.store_image(meta, resized, path=file)
        output_files.append(out_path)

    return {"output": output_files}


def resize_validate(input: List[str], width: int, height: int, meta: Metadata):
    if width <= 0 or height <= 0:
        raise ValueError("width and height must be positive")


@node(
    tasks={"exec": resize_exec, "validate": resize_validate},
    inputs={
        "input": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL", "default": list()},
        "width": {"type": "INT", "required": True, "widget": "INPUT", "default": 224},
        "height": {"type": "INT", "required": True, "widget": "INPUT", "default": 224},
    },
    outputs={"output": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL", "default": list()}},
    display_name="Resize",
    category="Preprocessing",
    description="Resize image to fixed size",
)
def resize(input: List[str], width=224, height=224, meta: Metadata = None):
    pass