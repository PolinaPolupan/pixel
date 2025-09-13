import inspect
from abc import ABC, abstractmethod
from typing import Any, Dict, List

from pixel.utils import map_input_params


class Node(ABC):
    node_type = None

    required_packages: List[str] = []

    @property
    def type(self) -> str:
        return self.__class__.node_type

    @classmethod
    def get_required_packages(cls) -> List[str]:
        return cls.required_packages

    @abstractmethod
    def get_input_types(self) -> Dict[str, Any]:
        pass

    @abstractmethod
    def get_output_types(self) -> Dict[str, Any]:
        pass

    @abstractmethod
    def get_display_info(self) -> Dict[str, str]:
        pass

    def exec_params(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        sig = inspect.signature(self.exec)
        return self.exec(**map_input_params(inputs, sig))

    @abstractmethod
    def exec(self, **kwargs) -> Dict[str, Any]:
        pass

    def validate_params(self, inputs: Dict[str, Any]) -> None:
        sig = inspect.signature(self.validate)
        return self.validate(**map_input_params(inputs, sig))

    @abstractmethod
    def validate(self, **kwargs) -> None:
        pass
