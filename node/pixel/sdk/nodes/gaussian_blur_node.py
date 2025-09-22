from typing import List
from pixel.core import Metadata
from pixel.sdk import Client
from pixel.sdk.models.node_decorator import node


def gaussian_blur_exec(
    input: List[str],
    sizeX: int,
    sizeY: int = 3,
    sigmaX: float = 0.0,
    sigmaY: float = 0.0,
    meta: Metadata = None
):
    output_files = []
    for file in input:
        output_files.append(
            Client.store_task(meta.task_id, meta.node_id, file)
        )
    return {"output": output_files}

def gaussian_blur_validate(
    input: List[str],
    sizeX: int,
    sizeY: int = 3,
    sigmaX: float = 0.0,
    sigmaY: float = 0.0,
    meta: Metadata = None
):
    try:
        sizeX = int(sizeX)
        sizeY = int(sizeY)
        sigmaX = float(sigmaX)
        sigmaY = float(sigmaY)
    except (TypeError, ValueError):
        raise ValueError("Invalid parameter types")
    if sizeX <= 0 or sizeX % 2 == 0:
        raise ValueError("sizeX must be positive and odd")
    if sizeY <= 0 or sizeY % 2 == 0:
        raise ValueError("sizeY must be positive and odd")
    if sigmaX < 0:
        raise ValueError("sigmaX must be non-negative")
    if sigmaY < 0:
        raise ValueError("sigmaY must be non-negative")

@node(
    tasks={"exec": gaussian_blur_exec, "validate": gaussian_blur_validate},
    inputs={
        "input": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL", "default": set()},
        "sizeX": {"type": "INT", "required": True, "widget": "INPUT", "default": 3},
        "sizeY": {"type": "INT", "required": False, "widget": "INPUT", "default": 3},
        "sigmaX": {"type": "DOUBLE", "required": False, "widget": "INPUT", "default": 0.0},
        "sigmaY": {"type": "DOUBLE", "required": False, "widget": "INPUT", "default": 0.0},
    },
    outputs={
        "output": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL"}
    },
    display_name="Gaussian Blur",
    category="Filtering",
    description="Blurs an image using a Gaussian kernel",
    color="#FF8A65",
    icon="BlurIcon"
)
def gaussian_blur(
    input: List[str],
    sizeX: int,
    sizeY: int = 3,
    sigmaX: float = 0.0,
    sigmaY: float = 0.0,
    meta: Metadata = None
):
    pass
