from typing import List
from pixel.sdk.models.node_decorator import node
from pixel.core import Metadata


@node(
    inputs={
        "input": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL", "default": list()}
    },
    outputs={
        "json": {"type": "STRING", "required": True}
    },
    display_name="ResNet50",
    category="ML",
    description="Run ResNet50 on images",
    color="#81C784",
    icon="ResNet50Icon"
)
def resnet50(input: List[str], meta: Metadata = None):
    outputs = {"json": ""}
    return outputs
