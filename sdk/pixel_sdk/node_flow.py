from pixel_sdk import Client, create_node

class NodeOutput:
    """
    Represents an output from a node that can be passed to other nodes.
    Automatically creates output references using the exact output keys from the server.
    """

    def __init__(self, node_id, node_type, flow):
        self.node_id = node_id
        self.node_type = node_type
        self.flow = flow
        self._output_keys = self._get_output_keys()

    def _get_output_keys(self):
        node_info = self.flow.available_node_types.get(self.node_type, {})
        outputs = node_info.get('outputs', {})
        return list(outputs.keys())

    def __str__(self):
        default_key = self._output_keys[0] if self._output_keys else "output"
        return f"@node:{self.node_id}:{default_key}"

    def __repr__(self):
        return f"NodeOutput(node_id={self.node_id}, type='{self.node_type}', keys={self._output_keys})"

    def __getattr__(self, name):
        if name in self._output_keys:
            return f"@node:{self.node_id}:{name}"
        available_keys = ", ".join(self._output_keys) if self._output_keys else "none"
        raise AttributeError(f"Node of type '{self.node_type}' has no output key '{name}'. "
                             f"Available output keys: {available_keys}")

class NodeFlow:
    _node_methods = {}

    def __init__(self):
        self.client = Client(engine_url="http://localhost:8080", node_url="http://localhost:8000")
        self.scene_id = None
        self.nodes = {}
        self.next_id = 1
        self.node_results = {}
        self.available_node_types = self._fetch_available_node_types()
        if not NodeFlow._node_methods:
            self._register_node_methods()

    def _fetch_available_node_types(self):
        try:
            return self.client.get_node_info()
        except Exception as e:
            print(f"Warning: Could not fetch node types from server: {e}")
            return {}

    def _create_node(self, node_type, kwargs):
        node_id = kwargs.pop('node_id', None) or self.next_id
        self.next_id += 1
        processed_inputs = dict(kwargs)
        node_def = create_node(
            node_id=node_id,
            node_type=node_type,
            inputs=processed_inputs
        )
        self.nodes[node_id] = node_def
        return NodeOutput(node_id, node_type, self)

    def _create_node_method(self, node_type):
        def node_method(inner_self, **kwargs):
            return self._create_node(node_type, kwargs)
        return node_method

    def _register_node_methods(self):
        if not self.available_node_types:
            print("Warning: No node types available to register")
            return
        for node_type in self.available_node_types:
            if node_type not in NodeFlow._node_methods:
                NodeFlow._node_methods[node_type] = self._create_node_method(node_type)

    def __getattr__(self, name):
        if name.startswith('_'):
            raise AttributeError(f"'{self.__class__.__name__}' object has no attribute '{name}'")
        if name in NodeFlow._node_methods:
            return NodeFlow._node_methods[name].__get__(self, self.__class__)
        if name in self.available_node_types:
            NodeFlow._node_methods[name] = self._create_node_method(name)
            return NodeFlow._node_methods[name].__get__(self, self.__class__)
        raise AttributeError(
            f"Node type '{name}' is not registered or available in this flow. "
            f"Available node types: {', '.join(self.available_node_types.keys())}"
        )

    def create_scene(self):
        self.scene_id = self.client.create_scene()
        print(f"Created new scene with ID: {self.scene_id}")
        return self.scene_id

    def upload_file(self, file_path: str):
        if not self.scene_id:
            self.create_scene()
        result = self.client.upload_file(scene_id=self.scene_id, file_path=file_path)
        print(f"Uploaded file: {file_path}")
        return result

    def list_files(self):
        if not self.scene_id:
            self.create_scene()
        return self.client.list_scene_files(self.scene_id).get("locations", [])

    def execute(self):
        if not self.scene_id:
            self.create_scene()
        nodes_list = list(self.nodes.values())
        print(f"Executing workflow...")
        print(f"Scene ID: {self.scene_id}")
        print(f"Number of nodes: {len(nodes_list)}")
        result = self.client.execute_scene(self.scene_id, nodes_list)
        print(f"Workflow execution completed")
        return result

    def download_file(self, file_path: str, save_to: str):
        if not self.scene_id:
            raise ValueError("No active scene. Create a scene first.")
        content = self.client.get_file(self.scene_id, file_path)
        with open(save_to, "wb") as f:
            f.write(content)
        print(f"Downloaded file to: {save_to}")
        return save_to