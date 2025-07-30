from node import Node
from typing import Any, Dict


class TestNode(Node):
    def get_input_types(self) -> Dict[str, Any]:
        return {"x": int, "y": int}

    def get_default_inputs(self) -> Dict[str, Any]:
        return {"x": 0, "y": 0}

    def get_output_types(self) -> Dict[str, Any]:
        return {"sum": int}

    def get_display_info(self) -> Dict[str, str]:
        return {"label": "Adder"}

    def exec(self) -> Dict[str, Any]:
        # Example: adds x and y
        x = self.inputs.get("x", 0)
        y = self.inputs.get("y", 0)
        return {"sum": x + y}

    def validate(self) -> None:
        x = self.inputs.get("x")
        y = self.inputs.get("y")
        if not isinstance(x, int) or not isinstance(y, int):
            raise ValueError("x and y must be integers")

def node_from_json(data: Dict[str, Any]) -> TestNode:
    return TestNode(
        id=data.get("id", 1),
        type=data.get("type", "test"),
        inputs=data.get("inputs", {})
    )