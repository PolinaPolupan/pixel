from dataclasses import dataclass


@dataclass
class Metadata:
    id: int
    scene_id: int
    task_id: int