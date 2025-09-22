from functools import wraps
from pixel.sdk import NodeFlow

def flow(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        nf = NodeFlow()
        nf.create_graph()

        injected = {ntype: getattr(nf, ntype) for ntype in nf.available_node_types}
        original_globals = func.__globals__.copy()
        func.__globals__.update(injected)

        try:
            result = func(*args, **kwargs)
        finally:
            func.__globals__.update(original_globals)

        execution_result = nf.execute()
        return result, execution_result, nf
    return wrapper
