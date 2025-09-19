# pixel
An image processing system designed to proccess images using node-based workflow

> ⚠️ **IMPORTANT**: This project is currently in early development phase

## Example workflow
![image](https://github.com/user-attachments/assets/a4c38118-e976-42d3-b599-abfa7d92621f)

```
from typing import List

from pixel.sdk import NodeFlow
from pixel.sdk.models.node_decorator import node


@node()
def custom_blur(input: List[str], ksize):
    """Blurs an image using the specified kernel size"""
    print("Hello I'm custom blur")
    return {"output": input, "sigma": 5, "ksize": ksize, "param": "style"}

flow = NodeFlow()

scene_id = flow.create_scene()

files = flow.list_files()

access_key_id = flow.string(input="key")
secret_access_key = flow.string(input="secret")
bucket = flow.string(input="bucket")
region = flow.string(input="region")


input_node = flow.input(input=files)
floor_result = flow.floor(input=56)

combined = flow.combine(
    files_0=input_node.output
)

blurred = flow.gaussian_blur(
    input=combined.output,
    sigmaX=floor_result.output,
    sizeX=33,
    sizeY=33
)

value = flow.floor(input=8)

custom = flow.custom_blur(
    input=blurred.output,
    ksize=value.output
)

output = flow.output(
    input=blurred.output,
    prefix="output1",
    folder="output_1"
)


execution_result = flow.execute()

print(flow.nodes)
print(flow.list_files())
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

# Custom Node Creation Guide

You can extend Pixel by adding your own custom nodes. This allows you to implement new functionality and integrate it into the Pixel engine.

## Steps to Create a Custom Node

1. **Create the Custom Nodes Folder**

   Create a directory named `custom_nodes` in the root of your project (next to your `docker-compose` file).

2. **Write a Node Python File**

   Inside `custom_nodes`, create a new Python file (e.g., `my_node.py`). Each file can contain one or more node classes.

3. **Define Your Node Class**

   Your node class should inherit from the base `Node` class and define a unique `node_type`. For example:

   ```python
   from node import Node

   class MyCustomNode(Node):
       node_type = "my_custom_node"
       required_packages = ["numpy"]  # Optional: list any extra pip packages

       def exec(self, data):
           # Your node logic here
           return data
   ```

4. **Optional: Specify Dependencies**

   If your node requires extra Python packages, list them in the `required_packages` attribute. The engine will attempt to install them automatically.

5. **Restart the Node Service**

   If you are using Docker Compose, restart the node service so your new nodes are discovered:

   ```bash
   docker compose restart node
   ```

6. **Verify**

   Your custom node should now be available in the Pixel engine.
   
---

## Troubleshooting

- Make sure Docker and Docker Compose are running.
- If you see "port already in use," change the port number in `docker-compose.yml`.
- For other issues, see our [GitHub Issues](https://github.com/PolinaPolupan/pixel/issues).

---

Enjoy using Pixel!

## Contributing

This project is in the initial planning and development phase. Contributors interested in collaborating on the architecture and core implementation are welcome to open issues for discussion.

## License

MIT license
