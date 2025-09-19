from pixel.core import Metadata
from pixel.sdk.models.node_decorator import node


@node(
    inputs={
        "input": {"type": "STRING", "required": True, "widget": "INPUT", "default": ""}
    },
    outputs={
        "output": {"type": "STRING", "required": True}
    },
    display_name="String",
    category="Types",
    description="String",
    color="#AED581",
    icon="StringIcon"
)
def string_node(input: str, meta: Metadata = None):
    return {"output": input}
