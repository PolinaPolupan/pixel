from src import Client, create_node

client = Client(base_url="http://localhost:8080")

scene_id = client.create_scene()
print(f"Created new scene with ID: {scene_id}")

with open("file.jpg", "rb") as f:
    uploaded_files = client.upload_file(
        scene_id=scene_id,
        file_content=f
    )
    print(f"Uploaded files: {uploaded_files}")

with open("file1.jpg", "rb") as f:
    uploaded_files = client.upload_file(
        scene_id=scene_id,
        file_content=f
    )
    print(f"Uploaded files: {uploaded_files}")

files = client.list_scene_files(scene_id)
print(f"Files in scene {scene_id}:")
for file in files:
    print(f"  - {file}")

nodes = [
    # S3 Input node
    create_node(
        node_id=0,
        node_type="S3Input",
        inputs={
            "access_key_id": "access",
            "secret_access_key": "secret",
            "region": "region",
            "bucket": "bucket"
        }
    ),

    # Input node
    create_node(
        node_id=10,
        node_type="Input",
        inputs={
            "input": [
                f"scenes/{scene_id}/file1.jpg",
                f"scenes/{scene_id}/file.jpg"
            ]
        }
    ),

    # Combine node
    create_node(
        node_id=11,
        node_type="Combine",
        inputs={
            "files_0": "@node:10:output",
            "files_1": "@node:0:output"
        }
    ),

    # Floor node
    create_node(
        node_id=4,
        node_type="Floor",
        inputs={
            "input": 56
        }
    ),

    # Gaussian Blur node
    create_node(
        node_id=1,
        node_type="GaussianBlur",
        inputs={
            "files": "@node:11:output",
            "sizeX": 33,
            "sizeY": 33,
            "sigmaX": "@node:4:output"
        }
    ),

    # Output node
    create_node(
        node_id=2,
        node_type="Output",
        inputs={
            "files": "@node:1:output",
            "prefix": "output1",
            "folder": "output_1"
        }
    ),

    # S3 Output node
    create_node(
        node_id=12,
        node_type="S3Output",
        inputs={
            "files": "@node:1:output",
            "folder": "output",
            "access_key_id": "access",
            "secret_access_key": "secret",
            "region": "region",
            "bucket": "bucket"
        }
    )
]

# Execute the scene with the configured nodes
result = client.execute_scene(scene_id, nodes)
print(f"Scene execution result: {result}")

# Download a processed file
output_file = client.get_file(scene_id, "file1.jpg")
with open("downloaded_result.png", "wb") as f:
    f.write(output_file)

print("Workflow completed successfully!")