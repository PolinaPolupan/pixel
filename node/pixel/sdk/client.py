import os
import requests
from typing import List, Dict, Any, Optional
from urllib.parse import urljoin

from pixel.server.load_nodes import NODE_REGISTRY


class Client:
    def __init__(self):
        self.engine_url = "http://engine:8080"
        self.session = requests.Session()

    def _make_engine_url(self, path: str) -> str:
        return urljoin(self.engine_url, path)

    def get_node_info(self) -> Dict[str, Any]:
        result = {}
        for node_type, node_cls in NODE_REGISTRY.items():
            node = node_cls()
            result[node_type] = node.metadata
        return result

    def create_scene(self) -> str:
        url = self._make_engine_url("/v1/scene/")
        response = self.session.post(url)
        response.raise_for_status()
        return response.json().get("id")

    def list_scene_files(self, scene_id: str) -> Dict[str, Any]:
        url = self._make_engine_url(f"/v1/scene/{scene_id}/list")
        response = self.session.get(url)
        response.raise_for_status()
        return response.json()

    from typing import BinaryIO

    def upload_file(self, filename: str, file_obj: BinaryIO, content_type: Optional[str] = None) -> Dict[str, Any]:
        url = self._make_engine_url(f"/v1/storage/upload")
        if not content_type:
            content_type = self._guess_content_type(filename)

        files = {'file': (filename, file_obj, content_type)}
        response = self.session.post(url, files=files)

        if response.status_code >= 400:
            print(f"Upload failed with status code: {response.status_code}")
            print(f"Response content: {response.text}")
            print(f"Request details: URL={url}, filename={filename}, content_type={content_type}")
        response.raise_for_status()
        return response.json()

    def get_file(self, scene_id: str, file_path: str) -> bytes:
        url = self._make_engine_url(f"/v1/scene/{scene_id}/file")
        params = {'filepath': file_path}
        response = self.session.get(url, params=params)
        response.raise_for_status()
        return response.content

    def execute_scene(self, scene_id: str, nodes: List[Dict[str, Any]]) -> Dict[str, Any]:
        url = self._make_engine_url(f"/v1/scene/{scene_id}/exec")
        payload = {"nodes": nodes}
        response = self.session.post(url, json=payload)
        response.raise_for_status()
        return response.json()

    @staticmethod
    def _guess_content_type(filename: str) -> str:
        ext = filename.lower()
        if ext.endswith('.zip'):
            return 'application/zip'
        elif ext.endswith(('.jpg', '.jpeg')):
            return 'image/jpeg'
        elif ext.endswith('.png'):
            return 'image/png'
        else:
            return 'application/octet-stream'


def create_node(node_id: int, node_type: str, inputs: Dict[str, Any]) -> Dict[str, Any]:
    return {
        "id": node_id,
        "type": node_type,
        "inputs": inputs
    }