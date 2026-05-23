from pixel.sdk.flow_decorator import flow
from pixel.sdk.models.node_decorator import node
from pixel.core import Metadata

import os
import socket
import sys
import time


@node(
    image="python:3.12-slim",
    outputs={
        "message": {
            "type": "STRING",
            "required": True
        }
    },
    display_name="Docker Debug",
    category="Debug"
)
def docker_debug(meta: Metadata = None):
    print("=== INSIDE CONTAINER ===", file=sys.stderr)
    print("HOSTNAME:", socket.gethostname(), file=sys.stderr)
    print("DOCKER:", os.path.exists("/.dockerenv"), file=sys.stderr)
    print("PWD:", os.getcwd(), file=sys.stderr)

    time.sleep(10)

    return {
        "message": f"container={socket.gethostname()}"
    }

@node(image="python:3.12-slim")
def custom_blur(input: List[str], ksize):
    """Blurs an image using the specified kernel size"""
    print("Hello I'm custom blur")
    time.sleep(15)
    return {"output": input, "sigma": 5, "ksize": ksize, "param": "style"}


@flow
def docker_test_flow():
    s3_files = s3_input(conn_id="my_s3")
    blurred = gaussian_blur(
        input=s3_files.files,
        sizeX=3,
        sizeY=3,
        sigmaX=9
    )
    classified = mobilenet_classify(
        input = blurred.output
    )
    debug = docker_debug()
    blur = custom_blur(input = blurred.output)


docker_test_flow(id="docker-test")