import logging
import os
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
import socket

from node import get_node_class, register_node_class
from nodes.bilateral_filter_node import BilateralFilterNode
from nodes.blur_node import BlurNode
from nodes.box_filter_node import BoxFilterNode
from nodes.combine_node import CombineNode
from nodes.floor_node import FloorNode
from nodes.gaussian_blur_node import GaussianBlurNode
from nodes.input_node import InputNode
from nodes.median_blur_node import MedianBlurNode
from nodes.output_file_node import OutputFileNode
from nodes.output_node import OutputNode
from nodes.resnet50_node import ResNet50Node
from nodes.s3_input_node import S3InputNode
from nodes.s3_output_node import S3OutputNode
from nodes.string_node import StringNode
from nodes.vector2d_node import Vector2DNode

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Starting node service and registering node types...")

    node_classes = [
        BlurNode, GaussianBlurNode, BilateralFilterNode, BoxFilterNode,
        CombineNode, FloorNode, InputNode, MedianBlurNode, StringNode,
        Vector2DNode, OutputNode, OutputFileNode, ResNet50Node,
        S3InputNode, S3OutputNode
    ]

    for node_class in node_classes:
        register_node_class(node_class)
        logger.info(f"Registered node type: {node_class.__name__}")

    hostname = socket.gethostname()
    local_ip = socket.gethostbyname(hostname)
    logger.info(f"Service running on host: {hostname}, IP: {local_ip}")

    logger.info("Node service is ready to accept connections")
    logger.info("Application startup complete")

    yield

    logger.info("Shutting down node service")


app = FastAPI(
    title="Node Processing Service",
    description="Service for processing different node types in a graph",
    version="1.0.0",
    lifespan=lifespan
)


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


@app.post("/validate")
async def validate(request: Request):
    try:
        data = await request.json()
        logger.info(f"Validating node: {data.get('meta', {}).get('type')}")

        inputs = data.get("inputs")
        node = get_node(data)
        node.validate_params(inputs)

        return {"status": "ok"}
    except Exception as e:
        logger.error(f"Validation error: {str(e)}", exc_info=True)
        return JSONResponse(
            content={"status": "not ok", "error": str(e)},
            status_code=400
        )


@app.post("/exec")
async def exec_node(request: Request):
    try:
        data = await request.json()
        logger.info(f"Executing node: {data.get('meta', {}).get('type')}")

        node = get_node(data)
        outputs = node.exec_params(data)

        return outputs
    except Exception as e:
        logger.error(f"Execution error: {str(e)}", exc_info=True)
        return JSONResponse(
            content={"error": str(e)},
            status_code=400
        )


@app.get("/info")
async def info():
    from node import NODE_REGISTRY
    nodes = list(NODE_REGISTRY.keys())
    logger.info(f"Info request: {len(nodes)} nodes registered")
    return {
        "registered_nodes": nodes
    }


@app.get("/health")
async def health():
    """Health check endpoint for monitoring and diagnostics"""
    from sys import version
    import socket

    hostname = socket.gethostname()
    local_ip = socket.gethostbyname(hostname)

    from node import NODE_REGISTRY
    nodes_count = len(NODE_REGISTRY)

    return {
        "status": "healthy",
        "python_version": version,
        "hostname": hostname,
        "local_ip": local_ip,
        "registered_nodes_count": nodes_count
    }

if __name__ == "__main__":
    import uvicorn

    port = int(os.environ.get("PORT", 8000))

    host = os.environ.get("HOST", "0.0.0.0")

    logger.info(f"Starting server on {host}:{port}")
    uvicorn.run("main:app", host=host, port=port, reload=False)