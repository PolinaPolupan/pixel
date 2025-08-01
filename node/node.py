from abc import ABC, abstractmethod
from typing import Any, Dict, Optional, Type

NODE_REGISTRY = {}

class Node(ABC):
    node_type = None

    @property
    def type(self) -> str:
        return self.__class__.node_type

    @abstractmethod
    def get_input_types(self) -> Dict[str, Any]:
        pass

    @abstractmethod
    def get_output_types(self) -> Dict[str, Any]:
        pass

    @abstractmethod
    def get_display_info(self) -> Dict[str, str]:
        pass

    @abstractmethod
    def exec(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        pass

    @abstractmethod
    def validate(self, inputs: Dict[str, Any]) -> None:
        pass

def get_node_class(node_type: str) -> Optional[Type[Node]]:
    return NODE_REGISTRY.get(node_type)

def register_node_class(cls):
    if hasattr(cls, 'node_type') and cls.node_type is not None:
        NODE_REGISTRY[cls.node_type] = cls
    else:
        cls.node_type = cls.__name__
        NODE_REGISTRY[cls.node_type] = cls

    return cls
