from pixel.core import Node


class StringNode(Node):
    node_type = "string"

    metadata = {
        "inputs": {
            "input": { "type": "STRING", "required": True, "widget": "INPUT", "default": "" }
        },
        "outputs": {
            "output": { "type": "STRING", "required": True }
        },
        "display": {
            "name": "String",
            "category": "Types",
            "description": "String",
            "color": "#AED581",
            "icon": "StringIcon"
        }
    }

    def exec(self, input: str):
        return {"output": input}
