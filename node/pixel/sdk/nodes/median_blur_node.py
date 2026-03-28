from typing import List
from pixel.core import Metadata
from pixel.sdk import Client
from pixel.sdk.models.node_decorator import node
import cv2


def median_blur_exec(input: List[str], ksize: int, meta: Metadata = None):
    output_files = []
    for file in input:
        img = Client.load_image(file)

        filtered = cv2.medianBlur(
            img,
            (sizeX, sizeY)
        )

        out_path = Client.store_image(meta, filtered, path=file)
        output_files.append(out_path)

    return {"output": output_files}

def median_blur_validate(input: List[str], ksize: int, meta: Metadata = None):
    try:
        ksize = int(ksize)
    except (TypeError, ValueError):
        raise ValueError("ksize must be an integer")
    if ksize < 2 or ksize % 2 == 0:
        raise ValueError("ksize must be greater than 1 and odd")

@node(
    tasks={"exec": median_blur_exec, "validate": median_blur_validate},
    inputs={
        "input": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL", "default": list()},
        "ksize": {"type": "INT", "required": True, "widget": "INPUT", "default": 3}
    },
    outputs={
        "output": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL"}
    },
    display_name="Median Blur",
    category="Filtering",
    description="Blurs an image using the specified kernel size",
    color="#FF8A65",
    icon="BlurIcon"
)
def median_blur(input: List[str], ksize: int = 3, meta: Metadata = None):
    pass
