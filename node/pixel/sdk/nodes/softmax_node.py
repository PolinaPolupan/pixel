from typing import List
import numpy as np
from pixel.sdk.models.node_decorator import node
from pixel.core import Metadata


def softmax(x: np.ndarray) -> np.ndarray:
    e = np.exp(x - np.max(x))
    return e / e.sum()


def softmax_exec(vectors: List[dict], meta: Metadata):
    out = []

    for item in vectors:
        logits = np.array(item["vector"], dtype=np.float32)

        if logits.ndim == 2 and logits.shape[0] == 1:
            logits = logits[0]

        probs = softmax(logits)

        out.append({
            "file": item["file"],
            "probs": probs.tolist()
        })

    return {"probs": out}




@node(
    tasks={"exec": softmax_exec},
    inputs={
        "vectors": {"type": "ANY", "required": True, "widget": "LABEL", "default": list()},
    },
    outputs={
        "probs": {"type": "DEFAULT", "required": True}
    },
    display_name="Softmax",
    category="ML"
)
def softmax_node(vectors, meta=None):
    pass
