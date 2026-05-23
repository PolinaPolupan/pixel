from typing import List

from pixel.sdk.flow_decorator import flow
from pixel.sdk.models.node_decorator import node
from pixel.sdk.nodes.combine_node import combine
from pixel.sdk.nodes.floor_node import floor
from pixel.sdk.nodes.gaussian_blur_node import gaussian_blur
from pixel.sdk.nodes.input_node import input_node
from pixel.sdk.nodes.output_node import output
from pixel.sdk.nodes.s3_input_node import s3_input
from pixel.sdk.nodes.s3_output_node import s3_output
from pixel.sdk.nodes.mobilenet_classify_node import mobilenet_classify


@node()
def custom_blur(input: List[str], ksize):
    """Blurs an image using the specified kernel size"""
    print("Hello I'm custom blur")
    return {"output": input, "sigma": 5, "ksize": ksize, "param": "style"}


@flow
def graph_workflow():
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

graph_workflow(id="my-custom-pipeline")