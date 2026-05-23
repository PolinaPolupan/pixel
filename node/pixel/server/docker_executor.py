import subprocess
import json
import os

def execute_in_docker(node, data):
    image = "deploy-node"

    print(f"[DOCKER] Running node {node.type} in image {image}")

    HOST_PROJECT_ROOT = os.environ["HOST_PROJECT_ROOT"]

    cmd = [
        "docker", "run",
        "--rm",
        "-i",

        "--network", "pixel-net",

        "-v", f"{HOST_PROJECT_ROOT}/node:/app/node",
        "-v", f"{HOST_PROJECT_ROOT}/execution_graph:/app/execution_graph",

        "-w", "/app/node",

        "-e", "PYTHONPATH=/app/node",
        "-e", "EXECUTION_GRAPH_DIR=/app/execution_graph",
        "-e", "ENGINE_SERVICE_URL=http://engine:8080",

        image,
        "python", "-m", "pixel.server.runner"
    ]

    result = subprocess.run(
        cmd,
        input=json.dumps(data),
        capture_output=True,
        text=True
    )

    print("[DOCKER STDOUT]")
    print(result.stdout)

    print("[DOCKER STDERR]")
    print(result.stderr)

    if result.returncode != 0:
        raise RuntimeError(result.stderr)

    return result.stdout