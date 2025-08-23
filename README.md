# pixel
An image processing system designed to proccess images using node-based workflow

> ⚠️ **IMPORTANT**: This project is currently in early development phase

## Example workflow
![image](https://github.com/user-attachments/assets/a4c38118-e976-42d3-b599-abfa7d92621f)

```
from src import NodeFlow

flow = NodeFlow()

scene_id = flow.create_scene()

flow.upload_file("test_res/file.jpg")
flow.upload_file("test_res/scene_1_files.zip")

files = flow.list_files()

access_key_id = flow.String(input="key")
secret_access_key = flow.String(input="secret")
bucket = flow.String(input="bucket")
region = flow.String(input="region")

s3_input = flow.S3Input(
    access_key_id=access_key_id.output,
    secret_access_key=secret_access_key.output,
    bucket=bucket.output,
    region=region.output
)

input_node = flow.Input(input=files)
floor_result = flow.Floor(input=56)

combined = flow.Combine(
    files_0=input_node.output,
    files_1=s3_input.files
)

blurred = flow.GaussianBlur(
    input=combined.output,
    sigmaX=floor_result.output,
    sizeX=33,
    sizeY=33
)

output = flow.Output(
    input=blurred.output,
    prefix="output1",
    folder="output_1"
)

s3_output = flow.S3Output(
    input=blurred.output,
    access_key_id=access_key_id.output,
    secret_access_key=secret_access_key.output,
    bucket=bucket.output,
    region=region.output,
    folder="output"
)

execution_result = flow.execute()

flow.download_file("file.jpg", "test_res/downloaded_result.png")
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

## Planned Tech Stack

- Java 17
- Spring Boot 3.4.3
- Apache Kafka
- OpenCV
- PostgreSQL
- Redis
- React (for web interface)

## Contributing

This project is in the initial planning and development phase. Contributors interested in collaborating on the architecture and core implementation are welcome to open issues for discussion.

## License

MIT license
