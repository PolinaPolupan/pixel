# pixel
An image processing system designed to proccess images using node-based workflow

> ⚠️ **IMPORTANT**: This project is currently in early development phase

## Example workflow
![image](https://github.com/user-attachments/assets/a4c38118-e976-42d3-b599-abfa7d92621f)

```
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


@node()
def custom_blur(input: List[str], ksize):
    """Blurs an image using the specified kernel size"""
    print("Hello I'm custom blur")
    return {"output": input, "sigma": 5, "ksize": ksize, "param": "style"}


@flow
def graph_workflow():
    s3_files = s3_input(conn_id="my_s3")
    files = input_node(input=["file.jpg", "file1.jpg"])
    combined_files = combine(
        files_0=files.output,
        files_1=s3_files.files
    )
    floored = floor(input=56)
    blurred = gaussian_blur(
        input=combined_files.output,
        sizeX=33,
        sizeY=33,
        sigmaX=floored.output
    )
    out1 = output(
        input=blurred.output,
        prefix="output1",
        folder="output_1"
    )
    s3_out = s3_output(
        conn_id="my_s3",
        input=blurred.output,
        folder="output"
    )
    custom = custom_blur(
        input=blurred.output,
        ksize=5
    )

graph_workflow(id="my-custom-pipeline")
```

## Core Features

- [x] Node-based workflow editor
- [x] OpenCV integration for high-performance image operations
- [x] Integration with cloud storage providers
- [x] Basic filters (blur, sharpen, edge detection, etc.)

## Future Roadmap

- [ ] GPU acceleration for compute-intensive operations
- [ ] Machine learning-based image analysis (object detection, face recognition)
- [ ] Advanced content-aware resizing and cropping
- [ ] Pre-built templates for common transformations
- [ ] Batch optimization for similar operations
- [ ] Comprehensive image metadata preservation
- [ ] OAuth2 authentication and fine-grained permissions

# Installation
---
## Prerequisites
- [Docker](https://docs.docker.com/get-docker/) installed
- [Docker Compose](https://docs.docker.com/compose/install/) (comes with Docker Desktop)

---

## Step 1: Create a Docker Compose File

Download **deploy/docker-compose.install.yaml** and **deploy/.env** from the repository

In a new folder, paste the files

---

## Step 2: Start the app

Open a terminal in this folder and run:

```sh
docker compose up
```

This command will download the pre-built images and start all services.

---

## Step 3: Access the App

- **Frontend:** [http://localhost:5173](http://localhost:5173)
- **Engine API:** [http://localhost:8080](http://localhost:8080)
- **Node API:** [http://localhost:8000](http://localhost:8000)

---

## Step 4: Stop

To stop all services, press `Ctrl+C` in the terminal or run:

```sh
docker compose down
```

---

## Optional: Use the Python SDK

You can interact with pixel using the Python SDK for automation and scripting.

### Install the SDK

```sh
pip install pixel-sdk
```

# Pipeline creation

You can create your pipelines using pixel sdk

1. **Create the Execution graph Folder**

   Create a directory named `execution_graph` in the root of your project (next to your `docker-compose` file).

2. **Write a Python File**

   Inside `execution_graph`, create a new Python file (e.g., `my_graph.py`).

---

## Contributing

This project is in the initial planning and development phase. Contributors interested in collaborating on the architecture and core implementation are welcome to open issues for discussion.

## License

MIT license
