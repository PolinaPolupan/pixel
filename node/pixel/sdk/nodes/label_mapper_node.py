import json
from pixel.sdk.models.node_decorator import node
from pixel.core import Metadata


def load_labels(path: str):
    with open(path) as f:
        return json.load(f)


def map_labels_exec(predictions, labels_path: str, meta: Metadata):
    labels = load_labels(labels_path)

    for item in predictions:
        for p in item["predictions"]:
            p["label"] = labels[p["class_id"]]

    return {"predictions": predictions}


@node(
    tasks={"exec": map_labels_exec},
    inputs={
        "predictions": {"type": "DEFAULT", "required": True, "widget": "LABEL", "default": list()},
        "labels_path": {"type": "STRING", "required": True, "widget": "INPUT", "default": "/app/models/imagenet_labels.json"}
    },
    outputs={"predictions": {"type": "DEFAULT"}}
)
def label_mapper(predictions, labels_path, meta=None):
    pass
