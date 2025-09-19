from typing import List
from pixel.core import Metadata
from pixel.sdk import StorageClient
from pixel.sdk.models.node_decorator import node


@node(
    inputs={
        "input": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL", "default": set()},
        "prefix": {"type": "STRING", "required": False, "widget": "INPUT", "default": ""},
        "folder": {"type": "STRING", "required": False, "widget": "INPUT", "default": ""}
    },
    outputs={},
    display_name="Output",
    category="IO",
    description="Output files to a folder",
    color="#AED581",
    icon="OutputIcon"
)
def output(input: List[str], prefix: str = "", folder: str = "", meta: Metadata = None):
    for filepath in input:
        StorageClient.store_from_workspace_to_scene(
            scene_id=meta.scene_id,
            source=filepath,
            folder=folder if folder else None,
            prefix=prefix if prefix else None
        )
    return {}