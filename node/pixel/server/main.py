import logging
import os
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
import socket

from starlette.middleware.cors import CORSMiddleware
import sys

from pixel.server.load_nodes import NODE_REGISTRY, load_nodes_from_directory, get_node

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
logger = logging.getLogger(__name__)

@asynccontextmanager
async def lifespan(app: FastAPI):
    load_nodes_from_directory(os.path.join(os.path.dirname(__file__), "../sdk/nodes"))
    print(f"Loaded nodes: {list(NODE_REGISTRY.keys())}")
    yield
    print("Shutting down")

app = FastAPI(
    title="Node Processing Service",
    description="Service for processing different node models in a graph",
    version="1.0.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.post("/validate")
async def validate(request: Request):
    try:
        data = await request.json()
        logger.info(f"Validating node: {data.get('meta', {}).get('type')}")

        inputs = data.get("inputs")
        node = get_node(data)
        node.validate_params(inputs)

        return {}
    except Exception as e:
        logger.error(f"Validation error: {str(e)}", exc_info=True)
        return JSONResponse(
            content={"error": str(e)},
            status_code=400
        )


@app.post("/exec")
async def exec_node(request: Request):
    try:
        data = await request.json()
        logger.info(f"Executing node: {data.get('meta', {}).get('type')}")

        node = get_node(data)
        outputs = node.exec_params(data)

        return {"outputs": outputs}
    except Exception as e:
        logger.error(f"Execution error: {str(e)}", exc_info=True)
        return JSONResponse(
            content={"error": str(e)},
            status_code=400
        )

@app.post("/load_nodes")
async def load_nodes_endpoint():
    try:
        load_nodes_from_directory(os.environ.get('EXECUTION_GRAPH_DIR'))
        return {"loaded_nodes": list(NODE_REGISTRY.keys())}
    except Exception as e:
        logger.error(f"Error loading nodes: {e}", exc_info=True)
        return JSONResponse(
            content={"error": str(e)},
            status_code=500
        )


@app.get("/info")
async def node_info():
    result = {}
    for node_type, node_cls in NODE_REGISTRY.items():
        node = node_cls()
        result[node_type] = node.metadata
    return result


@app.get("/health")
async def health():
    hostname = socket.gethostname()
    local_ip = socket.gethostbyname(hostname)

    nodes_count = len(NODE_REGISTRY)

    return {
        "status": "healthy",
        "python_version": sys.version,
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