from pixel.core import Node


class OutputFileNode(Node):
    node_type = "output_file"

    metadata = {
        "inputs": {
            "content": { "type": "STRING", "required": False, "widget": "INPUT", "default": "" },
            "filename": { "type": "STRING", "required": False, "widget": "INPUT", "default": "new.txt" }
        },
        "outputs": {},
        "display": {
            "name": "Output File",
            "category": "IO",
            "description": "Output to a file",
            "color": "#AED581",
            "icon": "OutputIcon"
        }
    }

    def exec(self, content, filename):
        return {}
