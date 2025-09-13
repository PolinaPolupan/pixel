import logging
import os
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
import socket

from starlette.middleware.cors import CORSMiddleware

from load_nodes import load_nodes_from_directory, NODE_REGISTRY, get_node
import sys

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Starting node service and registering node models...")

    load_nodes_from_directory(os.path.join(os.path.dirname(__file__), "pixel_sdk/nodes"))
    load_nodes_from_directory(os.environ.get('CUSTOM_NODES_DIR'))

    hostname = socket.gethostname()
    local_ip = socket.gethostbyname(hostname)
    logger.info(f"Service running on host: {hostname}, IP: {local_ip}")

    logger.info("Node service is ready to accept connections")
    logger.info("Application startup complete")

    yield

    logger.info("Shutting down node service")


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

        return {"status": "ok"}
    except Exception as e:
        logger.error(f"Validation error: {str(e)}", exc_info=True)
        return JSONResponse(
            content={"status": "not ok", "error": str(e)},
            status_code=400
        )


@app.post("/exec")
async def exec_node(request: Request):
    try:
        data = await request.json()
        logger.info(f"Executing node: {data.get('meta', {}).get('type')}")

        node = get_node(data)
        outputs = node.exec_params(data)

        return {"status": "ok", "outputs": outputs}
    except Exception as e:
        logger.error(f"Execution error: {str(e)}", exc_info=True)
        return JSONResponse(
            content={"error": str(e)},
            status_code=400
        )

@app.get("/info")
async def node_info():
    result = {}
    for node_type, node_cls in NODE_REGISTRY.items():
        node = node_cls()
        result[node_type] = {
            "inputs": node.get_input_types() if hasattr(node, "get_input_types") else {},
            "outputs": node.get_output_types() if hasattr(node, "get_output_types") else {},
            "display": node.get_display_info() if hasattr(node, "get_display_info") else {}
        }
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