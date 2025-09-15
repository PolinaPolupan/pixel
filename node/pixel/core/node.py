import inspect
from abc import ABC
from typing import Any, Dict, List

from pixel.core.metadata import Metadata


def map_input_params(inputs: Dict[str, Any], sig: inspect.Signature) -> Dict[str, Any]:
    input_data = inputs.get("inputs", inputs)
    meta_data = inputs.get("meta", {})

    meta = Metadata(
        id=meta_data.get("node_id"),
        scene_id=meta_data.get("scene_id"),
        task_id=meta_data.get("task_id")
    )

    params = {}

    for param_name, param in sig.parameters.items():
        if param_name == 'self':
            continue
        elif param_name == 'meta':
            params[param_name] = meta
        elif param_name in input_data:
            params[param_name] = input_data[param_name]
        elif param.default is not inspect.Parameter.empty:
            continue
        else:
            params[param_name] = None

    return params


class Node(ABC):
    node_type: str | None = None
    required_packages: List[str] = []
    metadata: Dict[str, Any] = {}

    @property
    def type(self) -> str | None:
        return self.__class__.node_type

    @classmethod
    def get_required_packages(cls) -> List[str]:
        return cls.required_packages

    def exec_params(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        sig = inspect.signature(self.exec)
        return self.exec(**map_input_params(inputs, sig))

    def exec(self, **kwargs) -> Dict[str, Any]:
        return {}

    def validate_params(self, inputs: Dict[str, Any]) -> None:
        sig = inspect.signature(self.validate)
        return self.validate(**map_input_params(inputs, sig))

    def validate(self, **kwargs) -> None:
        return None
