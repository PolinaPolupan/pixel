from pixel.sdk.models.node_decorator import node

@node(
    inputs={
        "content": {"type": "STRING", "required": False, "widget": "INPUT", "default": ""},
        "filename": {"type": "STRING", "required": False, "widget": "INPUT", "default": "new.txt"}
    },
    outputs={},
    display_name="Output File",
    category="IO",
    description="Output to a file",
    color="#AED581",
    icon="OutputIcon"
)
def output_file(content: str = "", filename: str = "new.txt", meta=None):
    return {}
