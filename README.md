# MyPixel
An image processing system designed to proccess images using node-based workflow

> ⚠️ **IMPORTANT**: This project is currently in early development phase

## Example workflow
![image](https://github.com/user-attachments/assets/a4c38118-e976-42d3-b599-abfa7d92621f)

```
{
  "nodes": [
    {
      "id": 0,
      "type": "Input",
      "inputs": {
        "files" : [
          "Picture1.png",
          "Picture3.png"
        ]
      }
    },
    {
      "id": 4,
      "type": "Floor",
      "inputs": {
        "number": 56
      }
    },
    {
      "id": 1,
      "type": "GaussianBlur",
      "inputs": {
        "files": "@node:0:files",
        "sizeX": 33,
        "sigmaX": "@node:4:number"
      }
    },
    {
      "id": 2,
      "type": "Output",
      "inputs": {
        "files": "@node:1:files",
        "prefix": "output"
      }
    },
    {
      "id": 3,
      "type": "S3Output",
      "inputs": {
        "files": "@node:1:files",
        "access_key_id": "ACCESS_KEY",
        "secret_access_key": "SECRET",
        "region": "REGION",
        "bucket": "BUCKET_NAME"
      }
    }
  ]
}
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
- [ ] Webhook notifications for job completion
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
