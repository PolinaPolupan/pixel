from typing import List
import cv2
import numpy as np
import onnxruntime as ort
import os

from pixel.core import Metadata
from pixel.sdk import Client
from pixel.sdk.models.node_decorator import node


_SESSIONS: dict[str, ort.InferenceSession] = {}


def get_session(model_path: str) -> ort.InferenceSession:
    if model_path not in _SESSIONS:
        _SESSIONS[model_path] = ort.InferenceSession(
            model_path,
            providers=["CPUExecutionProvider"]
        )
    return _SESSIONS[model_path]


def preprocess(img: np.ndarray, size: int) -> np.ndarray:
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img = cv2.resize(img, (size, size))

    img = img.astype(np.float32) / 255.0

    mean = np.array([0.485, 0.456, 0.406], dtype=np.float32)
    std = np.array([0.229, 0.224, 0.225], dtype=np.float32)

    img = (img - mean) / std
    img = np.transpose(img, (2, 0, 1))   # HWC → CHW
    img = np.expand_dims(img, axis=0)    # NCHW

    return img.astype(np.float32)


def onnx_image_exec(
    input: List[str],
    model_path: str,
    image_size: int,
    meta: Metadata
):
    session = get_session(model_path)

    input_name = session.get_inputs()[0].name
    output_name = session.get_outputs()[0].name

    outputs = []

    for path in input:
        img_bytes = Client.get_file(path)
        img = cv2.imdecode(
            np.frombuffer(img_bytes, np.uint8),
            cv2.IMREAD_COLOR
        )

        if img is None:
            outputs.append({
                "file": path,
                "error": "Failed to decode image"
            })
            continue

        x = preprocess(img, image_size)

        y = session.run([output_name], {input_name: x})[0]

        outputs.append({
            "file": path,
            "vector": y.tolist()  # JSON-safe
        })

    return {"vectors": outputs}


def onnx_image_validate(
    input: List[str],
    model_path: str,
    image_size: int,
    meta: Metadata
):
    if not input:
        raise ValueError("input must contain at least one file")

    if not os.path.exists(model_path):
        raise ValueError(f"Model not found: {model_path}")

    if image_size <= 0:
        raise ValueError("image_size must be > 0")


@node(
    tasks={
        "exec": onnx_image_exec,
        "validate": onnx_image_validate
    },
    inputs={
        "input": {
            "type": "FILEPATH_ARRAY",
            "required": True,
            "widget": "LABEL",
            "default": list()
        },
        "model_path": {
            "type": "STRING",
            "required": True,
            "widget": "INPUT",
            "default": "/app/models/mobilenetv2_100_Opset18.onnx"
        },
        "image_size": {
            "type": "INT",
            "required": False,
            "default": 224,
            "widget": "INPUT"
        }
    },
    outputs={
        "vectors": {
            "type": "DEFAULT",
            "required": True
        }
    },
    display_name="ONNX Image Inference",
    category="ML",
    description="Runs ONNX model on images and returns output vectors",
    color="#FF7043",
    icon="MLIcon"
)
def onnx_image_infer(
    input: List[str],
    model_path: str,
    image_size: int = 224,
    meta: Metadata = None
):
    pass
