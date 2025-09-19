from numbers import Number
from pixel.core import Metadata
from pixel.sdk.models import Vector2D
from pixel.sdk.models.node_decorator import node


@node(
    inputs={
        "x": {"type": "DOUBLE", "required": True, "widget": "INPUT", "default": 0.0},
        "y": {"type": "DOUBLE", "required": True, "widget": "INPUT", "default": 0.0}
    },
    outputs={
        "vector2D": {"type": "VECTOR2D", "required": True}
    },
    display_name="Vector2D",
    category="Types",
    description="Creates a 2D vector",
    color="#FF8A65",
    icon="Vector2DIcon"
)
def vector2d_node(x: Number = 0, y: Number = 0, meta: Metadata = None):
    vector2d = Vector2D(x, y)
    return {"vector2D": vector2d.to_dict()}
