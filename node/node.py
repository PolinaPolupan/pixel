from abc import ABC, abstractmethod
from typing import Any, Dict, Optional

class Node(ABC):
    def __init__(self, id: int, type: str, inputs: Optional[Dict[str, Any]] = None):
        self._id = id
        self.scene_id: Optional[int] = None
        self.task_id: Optional[int] = None
        self._type = type
        self.inputs: Dict[str, Any] = inputs or {}

    @property
    def id(self) -> int:
        return self._id

    @property
    def type(self) -> str:
        return self._type

    @abstractmethod
    def get_input_types(self) -> Dict[str, Any]:
        pass

    @abstractmethod
    def get_default_inputs(self) -> Dict[str, Any]:
        pass

    @abstractmethod
    def get_output_types(self) -> Dict[str, Any]:
        pass

    @abstractmethod
    def get_display_info(self) -> Dict[str, str]:
        pass

    @abstractmethod
    def exec(self) -> Dict[str, Any]:
        pass

    @abstractmethod
    def validate(self) -> None:
        pass