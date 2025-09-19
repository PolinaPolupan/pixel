from typing import Optional, Set
from pixel.core import Metadata
from pixel.sdk import StorageClient
from pixel.sdk.models.node_decorator import node


@node(
    inputs={
        "files_0": {"type": "FILEPATH_ARRAY", "required": True,  "widget": "LABEL", "default": set()},
        "files_1": {"type": "FILEPATH_ARRAY", "required": False, "widget": "LABEL", "default": set()},
        "files_2": {"type": "FILEPATH_ARRAY", "required": False, "widget": "LABEL", "default": set()},
        "files_3": {"type": "FILEPATH_ARRAY", "required": False, "widget": "LABEL", "default": set()},
        "files_4": {"type": "FILEPATH_ARRAY", "required": False, "widget": "LABEL", "default": set()},
    },
    outputs={
        "output": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL"}
    },
    display_name="Combine",
    category="IO",
    description="Combine multiple data sources into a single source",
    color="#AED581",
    icon="CombineIcon"
)
def combine(
    meta: Metadata,
    files_0: Optional[Set[str]] = None,
    files_1: Optional[Set[str]] = None,
    files_2: Optional[Set[str]] = None,
    files_3: Optional[Set[str]] = None,
    files_4: Optional[Set[str]] = None,
):
    files = set()
    for file_set in [files_0, files_1, files_2, files_3, files_4]:
        if file_set:
            if not isinstance(file_set, set):
                file_set = set(file_set) if isinstance(file_set, (list, tuple)) else {file_set}
            files.update(file_set)

    output_files = [
        StorageClient.store_from_workspace_to_task(meta.task_id, meta.node_id, file)
        for file in files
    ]

    return {"output": output_files}
