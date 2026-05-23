from pixel.sdk.flow_decorator import flow
from pixel.sdk.models.node_decorator import node
from pixel.core import Metadata

import os
import socket
import sys
import time


def docker_debug_exec(meta: Metadata = None):
    print("=== INSIDE CONTAINER ===", file=sys.stderr)
    print("HOSTNAME:", socket.gethostname(), file=sys.stderr)
    print("DOCKER:", os.path.exists("/.dockerenv"), file=sys.stderr)
    print("PWD:", os.getcwd(), file=sys.stderr)

    time.sleep(10)

    return {
        "message": f"container={socket.gethostname()}"
    }


@node(
    image="python:3.12-slim",
    outputs={
        "message": {
            "type": "STRING",
            "required": True
        }
    },
    display_name="Docker Debug",
    category="Debug",
    tasks={
        "exec": docker_debug_exec
    }
)
def docker_debug(meta: Metadata = None):
    pass


@flow
def docker_test_flow():
    debug = docker_debug()


docker_test_flow(id="docker-test")