from pixel.core import Node


class ResNet50Node(Node):
    node_type = "ResNet50"

    metadata = {
        "inputs": {
            "input": { "type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL", "default": set() }
        },
        "outputs": {
            "json": { "type": "STRING", "required": True }
        },
        "display": {
            "category": "ML",
            "description": "Run ResNet50 on images",
            "color": "#81C784",
            "icon": "ResNet50Icon"
        }
    }

    def exec(self, input):
        outputs = {}
        return outputs
