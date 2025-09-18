import inspect
import re
from typing import List, Callable, Type, Dict, Any, get_type_hints

from pixel.core import Node
from pixel.server.load_nodes import register_node_class


def node(display_name: str = None,
         category: str = None,
         description: str = None,
         color: str = "#808080",
         icon: str = None,
         required_packages: List[str] = None):
    def decorator(func: Callable) -> Type[Node]:
        func_node_type = func.__name__.lower()

        sig = inspect.signature(func)

        inputs = {}
        for param_name, param in sig.parameters.items():
            if param_name == 'meta':
                continue

            has_default = param.default != inspect.Parameter.empty
            default_value = param.default if has_default else None

            inputs[param_name] = {
                "type": "DEFAULT",
                "required": not has_default,
                "widget": "LABEL",
            }

            if has_default and default_value is not None:
                inputs[param_name]["default"] = default_value

        outputs = {}
        try:
            source = inspect.getsource(func)
            matches = re.findall(r'return\s*{([^}]*)}', source)
            if matches:
                for match in matches:
                    key_matches = re.findall(r'["\']([^"\']+)["\']', match)
                    for key in key_matches:
                        outputs[key] = {
                            "type": "DEFAULT",
                            "required": True,
                            "widget": "LABEL"
                        }
        except Exception as e:
            print(f"Error analyzing function source: {e}")

        auto_display_name = ' '.join(word.capitalize() for word in func.__name__.split('_'))
        node_metadata = {
            "inputs": inputs,
            "outputs": outputs,
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

            def exec(self, **kwargs):
                return func(**kwargs)

            def validate(self, **kwargs):
                return None

        FunctionNode.__name__ = func.__name__.capitalize()
        FunctionNode.__qualname__ = FunctionNode.__name__
        FunctionNode.__doc__ = func.__doc__
        FunctionNode.required_packages = required_packages or []

        try:
            register_node_class(FunctionNode)
            print(f"Successfully registered node: {func_node_type}")
            print(f"Node inputs: {inputs}")
            print(f"Node outputs: {outputs}")
        except Exception as e:
            print(f"Error registering node: {e}")

        return FunctionNode

    return decorator
