from pixel.core import Metadata
from typing import List

from pixel.sdk import Client
from pixel.sdk.models.node_decorator import node


def bilateral_exec(input: List[str], d: int, sigmaColor: float, sigmaSpace: float, meta: Metadata):
    output_files = []
    for file in input:
        output_files.append(Client.store_dump(meta, file))
    return {"output": output_files}

def bilateral_validate(input: List[str], d: int, sigmaColor: float, sigmaSpace: float, meta: Metadata):
    if d < 1:
        raise ValueError("d must be greater than 0")
    if sigmaColor <= 0 or sigmaSpace <= 0:
        raise ValueError("sigmaColor and sigmaSpace must be positive")

@node(
    tasks={"exec": bilateral_exec, "validate": bilateral_validate},
    inputs={
        "input": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL", "default": list()},
        "d": {"type": "INT", "required": True, "widget": "INPUT", "default": 9},
        "sigmaColor": {"type": "DOUBLE", "required": True, "widget": "INPUT", "default": 75.0},
        "sigmaSpace": {"type": "DOUBLE", "required": True, "widget": "INPUT", "default": 75.0}
    },
    outputs={"output": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL"}},
    display_name="Bilateral Filter",
    category="Filtering",
    description="Applies a bilateral filter to the input image.",
    color="#FF8A65",
    icon="BlurIcon"
)
def bilateral_filter(input: List[str], d: int = 9, sigmaColor: float = 75.0, sigmaSpace: float = 75.0, meta: Metadata = None):
    pass
