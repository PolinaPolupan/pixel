import logging

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

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

app = FastAPI()

@app.on_event("startup")
async def startup_event():
    register_node_class(BlurNode)
    register_node_class(GaussianBlurNode)
    register_node_class(BilateralFilterNode)
    register_node_class(BoxFilterNode)
    register_node_class(CombineNode)
    register_node_class(FloorNode)
    register_node_class(GaussianBlurNode)
    register_node_class(InputNode)
    register_node_class(MedianBlurNode)
    register_node_class(StringNode)
    register_node_class(Vector2DNode)
    register_node_class(OutputNode)
    register_node_class(OutputFileNode)
    register_node_class(ResNet50Node)
    register_node_class(S3InputNode)
    register_node_class(S3OutputNode)


logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class Settings:
    def __init__(self):
        self.storage_url = "http://engine:8080/v1/storage"

def get_node(data: dict):
    meta = data.get("meta", {})
    if not meta:
        raise ValueError("Meta information is required")

    node_type = meta.get("type")
    if not node_type:
        raise ValueError("Node type is required in meta")

    node_class = get_node_class(node_type)
    if node_class is None:
        raise ValueError(f"Unknown node type: {node_type}")

    return node_class()


@app.post("/validate")
async def validate(request: Request):
    try:
        data = await request.json()

        inputs = data.get("inputs")
        node = get_node(data)
        node.validate(inputs)

        return {"status": "ok"}
    except Exception as e:
        return JSONResponse(
            content={"status": "not ok", "error": str(e)},
            status_code=400
        )


@app.post("/exec")
async def exec_node(request: Request):
    try:
        data = await request.json()

        inputs = data.get("inputs")
        node = get_node(data)
        outputs = node.exec(inputs)

        return outputs
    except Exception as e:
        return JSONResponse(
            content={"error": str(e)},
            status_code=400
        )

@app.get("/info")
async def info():
    from node import NODE_REGISTRY

    return {
        "registered_nodes": list(NODE_REGISTRY.keys())
    }