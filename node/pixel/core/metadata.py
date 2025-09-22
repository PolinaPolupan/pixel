from dataclasses import dataclass


@dataclass
class Metadata:
    node_id: int
    graph_id: int
    task_id: int