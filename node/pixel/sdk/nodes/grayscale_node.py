from typing import List
from pixel.core import Metadata
from pixel.sdk import Client
from pixel.sdk.models.node_decorator import node
import cv2


def grayscale_exec(input: List[str], meta: Metadata):
    output_files = []

    for file in input:
        img = Client.load_image(file)

        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

        out_path = Client.store_image(meta, gray, path=file)
        output_files.append(out_path)

    return {"output": output_files}


@node(
    tasks={"exec": grayscale_exec},
    inputs={"input": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL", "default": list()}},
    outputs={"output": {"type": "FILEPATH_ARRAY", "required": True}},
    display_name="Grayscale",
    category="Preprocessing",
)
def grayscale(input: List[str], meta: Metadata = None):
    pass