import inspect
import re
from typing import List, Callable, Type, Dict, Any
from pixel.core import Node
from pixel.server.load_nodes import register_node_class

def node(
    display_name: str = None,
    category: str = None,
    description: str = None,
    color: str = "#808080",
    icon: str = None,
    required_packages: List[str] = None,
    inputs: Dict[str, Dict[str, Any]] = None,
    outputs: Dict[str, Dict[str, Any]] = None,
    tasks: Dict[str, Callable] = None
):
    def decorator(func: Callable) -> Type[Node]:
        func_node_type = func.__name__.lower()
        sig = inspect.signature(func)

        node_inputs = inputs or {}
        if not inputs:
            for param_name, param in sig.parameters.items():
                if param_name == 'meta':
                    continue
                has_default = param.default != inspect.Parameter.empty
                default_value = param.default if has_default else None
                node_inputs[param_name] = { "type": "DEFAULT", "required": not has_default, "widget": "LABEL" }
                if has_default and default_value is not None:
                    node_inputs[param_name]["default"] = default_value

        node_outputs = outputs or {}
        if not outputs:
            try:
                source = inspect.getsource(func)
                matches = re.findall(r'return\s*{([^}]*)}', source)
                if matches:
                    for match in matches:
                        key_matches = re.findall(r'["\']([^"\']+)["\']', match)
                        for key in key_matches:
                            node_outputs[key] = { "type": "DEFAULT", "required": True, "widget": "LABEL" }
            except Exception as e:
                print(f"Error analyzing function source: {e}")

        auto_display_name = ' '.join(word.capitalize() for word in func.__name__.split('_'))
        node_metadata = {
            "inputs": node_inputs,
            "outputs": node_outputs,
            "display": {
                "name": display_name or auto_display_name,
                "category": category or "Other",
                "description": description or func.__doc__ or f"Executes {func.__name__}",
                "color": color,
                "icon": icon or f"{func.__name__}Icon"
            }
        }

        class FunctionNode(Node):
            node_type = func_node_type
            metadata = node_metadata
            original_func = func
            original_signature = sig

            def __get_task_method(self, name: str):
                if tasks and name in tasks:
                    return tasks[name]
                if name == "exec":
                    return func
                return lambda **kwargs: None

            def exec(self, **kwargs):
                return self.__get_task_method("exec")(**kwargs)

            def validate(self, **kwargs):
                return self.__get_task_method("validate")(**kwargs)

        FunctionNode.__name__ = func.__name__.capitalize()
        FunctionNode.__qualname__ = FunctionNode.__name__
        FunctionNode.__doc__ = func.__doc__
        FunctionNode.required_packages = required_packages or []

        try:
            register_node_class(FunctionNode)
            print(f"Successfully registered node: {func_node_type}")
        except Exception as e:
            print(f"Error registering node: {e}")

        return FunctionNode

    return decorator
