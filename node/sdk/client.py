import os
import requests
from typing import List, Dict, Any, Optional
from urllib.parse import urljoin


class Client:
    def __init__(
        self,
        engine_url: Optional[str] = None,
        node_url: Optional[str] = None,
    ):
        self.engine_url = engine_url or os.getenv("ENGINE_SERVICE_URL", "http://engine:8080")
        self.node_url = node_url or os.getenv("NODE_SERVICE_URL", "http://node:8000")
        self.session = requests.Session()

    def _make_engine_url(self, path: str) -> str:
        return urljoin(self.engine_url, path)

    def _make_node_url(self, path: str) -> str:
        return urljoin(self.node_url, path)

    def get_node_info(self) -> Dict[str, Any]:
        url = self._make_node_url("/info")
        response = self.session.get(url)
        response.raise_for_status()
        return response.json()

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

    def upload_file(self, scene_id: str, file_path: str) -> Dict[str, Any]:
        url = self._make_engine_url(f"/v1/scene/{scene_id}/upload")
        filename = os.path.basename(file_path)
        content_type = self._guess_content_type(filename)

        with open(file_path, "rb") as file_content:
            files = {'file': (filename, file_content, content_type)}
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