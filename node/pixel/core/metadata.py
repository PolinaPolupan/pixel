from dataclasses import dataclass


@dataclass
class Metadata:
    node_id: int
    scene_id: int
    task_id: int