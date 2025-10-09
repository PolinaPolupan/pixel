from pixel.sdk import NodeFlow
from functools import wraps

def flow(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        nf = NodeFlow()

        for ntype in nf.available_node_types:
            func.__globals__[ntype] = getattr(nf, ntype)

        id = kwargs.pop("id", None)

        result = func(*args, **kwargs)

        if nf.nodes:
            nf.create_graph(id=id)

        return result

    return wrapper