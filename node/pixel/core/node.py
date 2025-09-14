import inspect
from abc import ABC
from typing import Any, Dict, List

from pixel.core import map_input_params


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
