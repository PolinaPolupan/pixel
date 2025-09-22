from typing import List
from pixel.core import Metadata
from pixel.sdk import Client
from pixel.sdk.models.node_decorator import node


def box_filter_exec(input: List[str], ksize, ddepth, meta: Metadata):
    output_files = []
    for file in input:
        output_files.append(
            Client.store_task(meta.task_id, meta.node_id, file)
        )
    return {"output": output_files}

def box_filter_validate(input: List[str], ksize, ddepth, meta: Metadata):
    if isinstance(ksize, dict):
        x = ksize.get("x", 0)
        y = ksize.get("y", 0)
    else:
        x = getattr(ksize, "x", 0)
        y = getattr(ksize, "y", 0)

    try:
        x = int(x)
        y = int(y)
    except (TypeError, ValueError):
        raise ValueError("KSize values must be convertible to integers")

    if x < 1 or y < 1:
        raise ValueError("KSize must be greater than 0")

@node(
    tasks={"exec": box_filter_exec, "validate": box_filter_validate},
    inputs={
        "input": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL", "default": set()},
        "ddepth": {"type": "INT", "required": True, "widget": "INPUT", "default": 0},
        "ksize": {"type": "VECTOR2D", "required": True, "widget": "LABEL", "default": {"x": 1, "y": 1}}
    },
    outputs={
        "output": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL"}
    },
    display_name="Box Filter",
    category="Filtering",
    description="Blurs an image using the specified kernel size",
    color="#FF8A65",
    icon="BlurIcon"
)
def box_filter(input: List[str], ksize={"x": 1, "y": 1}, ddepth: int = 0, meta: Metadata = None):
    pass
