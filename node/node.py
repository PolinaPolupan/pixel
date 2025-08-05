import inspect
from abc import ABC, abstractmethod
from inspect import Signature
from typing import Any, Dict, Optional, Type

from metadata import Metadata

NODE_REGISTRY = {}


def map_params(inputs: Dict[str, Any], sig: Signature) -> Dict[str, Any]:
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

    def exec_params(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        sig = inspect.signature(self.exec)
        return self.exec(**map_params(inputs, sig))

    @abstractmethod
    def exec(self, **kwargs) -> Dict[str, Any]:
        pass

    def validate_params(self, inputs: Dict[str, Any]) -> None:
        sig = inspect.signature(self.validate)
        return self.validate(**map_params(inputs, sig))

    @abstractmethod
    def validate(self, **kwargs) -> None:
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
