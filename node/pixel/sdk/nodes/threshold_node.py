from typing import List
from pixel.core import Metadata
from pixel.sdk import Client
from pixel.sdk.models.node_decorator import node
import cv2


def threshold_exec(input: List[str], thresh: float, meta: Metadata):
    output_files = []

    thresh = float(thresh)

    for file in input:
        img = Client.load_image(file)

        if len(img.shape) == 3:
            img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

        _, binary = cv2.threshold(img, thresh, 255, cv2.THRESH_BINARY)

        out_path = Client.store_image(meta, binary, path=file)
        output_files.append(out_path)

    return {"output": output_files}


@node(
    tasks={"exec": threshold_exec},
    inputs={
        "input": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL", "default": list()},
        "thresh": {"type": "DOUBLE", "required": True, "widget": "INPUT", "default": 127.0},
    },
    outputs={"output": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL", "default": list()}},
    display_name="Threshold",
    category="Preprocessing",
)
def threshold(input: List[str], thresh=127.0, meta: Metadata = None):
    pass