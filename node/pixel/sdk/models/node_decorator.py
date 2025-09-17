from typing import List, Callable, Type

from pixel.core import Node


def node(display_name: str = None,
         category: str = None,
         description: str = None,
         color: str = "#808080",
         icon: str = None,
         required_packages: List[str] = None):
    """
    Minimal decorator to convert a function into a Node class.

    @node(display_name="Blur", category="Filtering")
    def blur(input: List[str], ksize, meta: Metadata):
        # Implementation
        return {"output": output_files}
    """

    def decorator(func: Callable) -> Type[Node]:
        func_node_type = func.__name__.lower()

        node_metadata = {
            "inputs": {},
            "outputs": {},
            "display": {
                "name": display_name or func.__name__,
                "category": category or "Other",
                "description": description or func.__doc__ or f"Executes {func.__name__}",
                "color": color,
                "icon": icon or f"{func.__name__}Icon"
            }
        }

        class FunctionNode(Node):
            node_type = func_node_type
            metadata = node_metadata

            def exec(self, **kwargs):
                return func(**kwargs)

            def validate(self, **kwargs):
                return None

        FunctionNode.__name__ = func.__name__.capitalize()
        FunctionNode.__qualname__ = FunctionNode.__name__
        FunctionNode.__doc__ = func.__doc__
        FunctionNode.required_packages = required_packages or []

        return FunctionNode

    return decorator