import inspect
from inspect import Signature
from typing import Dict, Any

from pixel.models.metadata import Metadata


def map_input_params(inputs: Dict[str, Any], sig: Signature) -> Dict[str, Any]:
    input_data = inputs.get("inputs", inputs)
    meta_data = inputs.get("meta", {})

    meta = Metadata(
        id=meta_data.get("node_id"),
        scene_id=meta_data.get("scene_id"),
        task_id=meta_data.get("task_id")
    )

    params = {}

    for param_name, param in sig.parameters.items():
        if param_name == 'self':
            continue
        elif param_name == 'meta':
            params[param_name] = meta
        elif param_name in input_data:
            params[param_name] = input_data[param_name]
        elif param.default is not inspect.Parameter.empty:
            continue
        else:
            params[param_name] = None

    return params
