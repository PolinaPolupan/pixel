from dataclasses import dataclass


@dataclass
class Metadata:
    node_id: int
    graph_execution_id: int