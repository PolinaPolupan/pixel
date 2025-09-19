import math
from pixel.core import Metadata
from pixel.sdk.models.node_decorator import node


@node(
    inputs={
        "input": {"type": "DOUBLE", "required": True, "widget": "INPUT", "default": 0.0}
    },
    outputs={
        "output": {"type": "DOUBLE", "required": True, "widget": "LABEL"}
    },
    display_name="Floor",
    category="Math",
    description="Returns the largest integer less than or equal to the input number.",
    color="#BA68C8",
    icon="FloorIcon"
)
def floor(input=0.0, meta: Metadata = None):
    try:
        number = float(input)
    except (TypeError, ValueError):
        number = 0.0
    return {"output": math.floor(number)}
