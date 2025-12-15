from typing import List
import cv2
import numpy as np
import json
import os

from pixel.core import Metadata
from pixel.sdk import Client
from pixel.sdk.models.node_decorator import node
from pixel.server.main import SESSION, OUTPUT_NAME, INPUT_NAME


def load_imagenet_labels() -> List[str]:
    labels_path = os.path.join(os.path.dirname(__file__), "/app/models/imagenet_labels.json")
    try:
        with open(labels_path, 'r') as f:
            labels = json.load(f)
            return labels
    except FileNotFoundError:
        print(f"Warning: {labels_path} not found. Using class IDs only.")
        return []
    except Exception as e:
        print(f"Error loading labels: {e}")
        return []


IMAGENET_LABELS = load_imagenet_labels()


def get_class_name(class_id: int) -> str:
    if 0 <= class_id < len(IMAGENET_LABELS):
        return IMAGENET_LABELS[class_id]
    return f"class_{class_id}"


def preprocess(img: np.ndarray) -> np.ndarray:
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img = cv2.resize(img, (224, 224))

    img = img.astype(np.float32) / 255.0

    mean = np.array([0.485, 0.456, 0.406], dtype=np.float32)
    std = np.array([0.229, 0.224, 0.225], dtype=np.float32)

    img = (img - mean) / std

    img = np.transpose(img, (2, 0, 1))  # HWC → CHW
    img = np.expand_dims(img, axis=0)

    return img.astype(np.float32)


def softmax(x):
    e = np.exp(x - np.max(x))
    return e / e.sum()


def classify_exec(
        input: List[str],
        topk: int,
        confidence_threshold: float,
        meta: Metadata
):
    results = []

    for path in input:
        try:
            # Load image
            img_bytes = Client.get_file(path)
            img = cv2.imdecode(
                np.frombuffer(img_bytes, np.uint8),
                cv2.IMREAD_COLOR
            )

            if img is None:
                results.append({
                    "file": path,
                    "error": "Failed to decode image",
                    "predictions": []
                })
                continue

            x = preprocess(img)
            logits = SESSION.run([OUTPUT_NAME], {INPUT_NAME: x})[0][0]

            probs = softmax(logits)

            top_idx = probs.argsort()[-topk:][::-1]

            predictions = []
            for i in top_idx:
                score = float(probs[i])

                if score >= confidence_threshold:
                    predictions.append({
                        "class_id": int(i),
                        "class_name": get_class_name(int(i)),
                        "confidence": score,
                        "percentage": f"{score * 100:.2f}%"
                    })

            if not predictions and len(top_idx) > 0:
                i = top_idx[0]
                score = float(probs[i])
                predictions.append({
                    "class_id": int(i),
                    "class_name": get_class_name(int(i)),
                    "confidence": score,
                    "percentage": f"{score * 100:.2f}%"
                })

            results.append({
                "file": path,
                "predictions": predictions
            })

        except Exception as e:
            results.append({
                "file": path,
                "error": str(e),
                "predictions": []
            })

    return {"predictions": results}


def classify_validate(
        input: List[str],
        topk: int,
        confidence_threshold: float,
        meta: Metadata
):
    """Validate input parameters"""
    if not input or len(input) == 0:
        raise ValueError("input must contain at least one file path")

    if topk < 1 or topk > 1000:
        raise ValueError("topk must be between 1 and 1000")

    if confidence_threshold < 0.0 or confidence_threshold > 1.0:
        raise ValueError("confidence_threshold must be between 0.0 and 1.0")


@node(
    tasks={"exec": classify_exec, "validate": classify_validate},
    inputs={
        "input": {
            "type": "FILEPATH_ARRAY",
            "required": True,
            "widget": "LABEL",
            "default": list()
        },
        "topk": {
            "type": "INT",
            "required": True,
            "default": 5,
            "widget": "INPUT"
        },
        "confidence_threshold": {
            "type": "FLOAT",
            "required": False,
            "default": 0.1,
            "widget": "INPUT"
        }
    },
    outputs={
        "predictions": {
            "type": "STRING",
            "required": True
        }
    },
    display_name="Image Classification (MobileNetV2)",
    category="ML",
    description="Classifies images using MobileNetV2 (ImageNet-1k)",
    color="#64B5F6",
    icon="MLIcon"
)
def mobilenet_classify(
        input: List[str],
        topk: int = 5,
        confidence_threshold: float = 0.1,
        meta: Metadata = None
):
    pass