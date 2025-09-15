import importlib.util
import os
import logging
import subprocess
import sys

from pixel.core import Node

logger = logging.getLogger(__name__)

NODE_REGISTRY = {}

def install_package(package: str):
    try:
        logger.info(f"Installing package: {package}")
        subprocess.check_call([sys.executable, "-m", "pip", "install", package])
    except subprocess.CalledProcessError as e:
        logger.error(f"Failed to install package {package}: {str(e)}")


def install_dependencies(node_class):
    if not hasattr(node_class, 'required_packages') or not node_class.required_packages:
        return None

    logger.info(f"Installing dependencies for node {node_class.node_type}: {node_class.required_packages}")

    for package in node_class.required_packages:
        install_package(package)
    return None


def get_node_class(node_type: str):
    node_class = NODE_REGISTRY.get(node_type)
    return node_class


def register_node_class(cls):
    if hasattr(cls, 'node_type') and cls.node_type is not None:
        NODE_REGISTRY[cls.node_type] = cls
        install_dependencies(cls)

    return cls


def load_nodes_from_directory(directory: str):
    loaded = 0
    if not os.path.exists(directory):
        logger.warning(f"Node folder {directory} does not exist")
        return

    for filename in os.listdir(directory):
        if filename.endswith(".py") and not filename.startswith("__"):
            path = os.path.join(directory, filename)
            logger.info(f"Loading node: {path}")

            try:
                spec = importlib.util.spec_from_file_location(filename[:-3], path)
                module = importlib.util.module_from_spec(spec)
                spec.loader.exec_module(module)

                for attr_name in dir(module):
                    obj = getattr(module, attr_name)
                    if isinstance(obj, type) and issubclass(obj, Node) and obj:
                        register_node_class(obj)

                loaded += 1
            except Exception as e:
                logger.error(f"Error loading node from {path}: {str(e)}")

    logger.info(f"Loaded {loaded} node(s) from {directory}")

def get_node(data: dict):
    meta = data.get("meta", {})
    if not meta:
        logger.error("Request missing meta information")
        raise ValueError("Meta information is required")

    node_type = meta.get("type")
    if not node_type:
        logger.error("Request missing node type in meta")
        raise ValueError("Node type is required in meta")

    node_class = get_node_class(node_type)
    if node_class is None:
        logger.error(f"Unknown node type requested: {node_type}")
        raise ValueError(f"Unknown node type: {node_type}")

    logger.info(f"Creating node of type: {node_type}")
    return node_class()
