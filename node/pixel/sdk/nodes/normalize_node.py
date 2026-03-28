from typing import List
from pixel.core import Metadata
from pixel.sdk import Client
from pixel.sdk.models.node_decorator import node
import cv2


def normalize_exec(input: List[str], meta: Metadata):
    output_files = []

    for file in input:
        img = Client.load_image(file)

        norm = img.astype("float32") / 255.0

        out_path = Client.store_image(meta, norm, path=file)
        output_files.append(out_path)

    return {"output": output_files}


@node(
    tasks={"exec": normalize_exec},
    inputs={"input": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL", "default": list()}},
    outputs={"output": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL", "default": list()}},
    display_name="Normalize",
    category="Preprocessing",
    description="Scale pixels to [0,1]",
)
def normalize(input: List[str], meta: Metadata = None):
    pass