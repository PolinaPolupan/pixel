import sys
import json
import socket
import os

from pixel.server.load_nodes import (
    get_node,
    load_nodes_from_directory
)

def main():
    print("=== DOCKER RUNNER ===", file=sys.stderr)
    print("HOST:", socket.gethostname(), file=sys.stderr)
    print("DOCKER:", os.path.exists("/.dockerenv"), file=sys.stderr)

    load_nodes_from_directory("/app/node/pixel/sdk/nodes")

    graph_dir = os.environ.get(
        "EXECUTION_GRAPH_DIR",
        "/app/execution_graph"
    )

    load_nodes_from_directory(graph_dir)

    data = json.load(sys.stdin)

    node = get_node(data)

    outputs = node.exec_params(data)

    print(outputs)


if __name__ == "__main__":
    main()