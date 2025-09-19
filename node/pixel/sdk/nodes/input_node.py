from typing import Set
from pixel.core import Metadata
from pixel.sdk import StorageClient
from pixel.sdk.models.node_decorator import node

@node(
    inputs={
        "input": {"type": "FILEPATH_ARRAY", "required": True, "widget": "FILE_PICKER", "default": set()}
    },
    outputs={
        "output": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL"}
    },
    display_name="Input",
    category="IO",
    description="Input files",
    color="#AED581",
    icon="InputIcon"
)
def input_node(input: Set[str] = set(), meta: Metadata = None):
    output_files = []
    for file in input:
        output_files.append(
            StorageClient.store_from_workspace_to_task(meta.task_id, meta.node_id, file)
        )
    return {"output": output_files}
