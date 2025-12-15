from typing import List
import numpy as np
from pixel.sdk.models.node_decorator import node
from pixel.core import Metadata


def topk_exec(probs: List[dict], k: int, threshold: float, meta: Metadata):
    out = []

    for item in probs:
        p = np.array(item["probs"], dtype=np.float32)

        idx = p.argsort()[-k:][::-1]

        preds = []
        for i in idx:
            if p[i] >= threshold:
                preds.append({
                    "class_id": int(i),
                    "score": float(p[i])
                })

        out.append({
            "file": item["file"],
            "predictions": preds
        })

    return {"predictions": out}


@node(
    tasks={"exec": topk_exec},
    inputs={
        "probs": {"type": "ANY", "required": True, "widget": "LABEL", "default": list()},
        "k": {"type": "INT", "default": 5, "widget": "INPUT"},
        "threshold": {"type": "FLOAT", "default": 0.0, "widget": "INPUT"}
    },
    outputs={
        "predictions": {"type": "DEFAULT", "required": True}
    },
    display_name="Top-K",
    category="ML"
)
def topk_node(probs, k=5, threshold=0.0, meta=None):
    pass
