import importlib.util
import os
import logging
from node import register_node_class, Node

logger = logging.getLogger(__name__)

def load_nodes_from_directory(directory: str):
    loaded = 0
    if not os.path.exists(directory):
        logger.warning(f"Node folder {directory} does not exist")
        return
    for filename in os.listdir(directory):
        if filename.endswith(".py") and not filename.startswith("__"):
            path = os.path.join(directory, filename)
            logger.info(f"Loading node: {path}")
            spec = importlib.util.spec_from_file_location(filename[:-3], path)
            module = importlib.util.module_from_spec(spec)
            spec.loader.exec_module(module)
            for attr_name in dir(module):
                obj = getattr(module, attr_name)
                if isinstance(obj, type) and issubclass(obj, Node) and obj is not Node:
                    register_node_class(obj)
            loaded += 1
    logger.info(f"Loaded {loaded} node(s) from {directory}")

