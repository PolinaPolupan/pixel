from src import Client, create_node

client = Client(base_url="http://localhost:8080")

scene_id = client.create_scene()
print(f"Created new scene with ID: {scene_id}")

uploaded_files = client.upload_file(
    scene_id=scene_id,
    file_path="file.jpg"
)
print(f"Uploaded files: {uploaded_files}")

uploaded_files = client.upload_file(
    scene_id=scene_id,
    file_path="scene_1_files.zip"
)
print(f"Uploaded files: {uploaded_files}")

files = client.list_scene_files(scene_id).get("locations")
print(f"Files in scene {scene_id}:")
for file in files:
    print(f"  - {file}")

nodes = [
    # Input node
    create_node(
        node_id=10,
        node_type="Input",
        inputs={
            "input": files
        }
    ),

    # Combine node
    create_node(
        node_id=11,
        node_type="Combine",
        inputs={
            "files_0": "@node:10:output"
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
            "input": "@node:11:output",
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
            "input": "@node:1:output",
            "prefix": "output1",
            "folder": "output_1"
        }
    )
]

# Execute the scene with the configured nodes
result = client.execute_scene(scene_id, nodes)
print(f"Scene execution result: {result}")

# Download a processed file
output_file = client.get_file(scene_id, "file.jpg")
with open("downloaded_result.png", "wb") as f:
    f.write(output_file)

print("Workflow completed successfully!")