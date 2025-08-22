import os
import requests
from typing import List, Dict, Any, Optional, Union, BinaryIO
from urllib.parse import urljoin


class Client:
    def __init__(self, base_url: str = "http://localhost:8080"):
        self.base_url = base_url
        self.session = requests.Session()

    def _make_url(self, path: str) -> str:
        return urljoin(self.base_url, path)

    def get_node_info(self) -> Dict[str, Any]:
        response = self.session.get(self._make_url("/info"))
        response.raise_for_status()
        return response.json()

    def create_scene(self) -> str:
        response = self.session.post(self._make_url("/v1/scene/"))
        response.raise_for_status()
        return response.json().get("id")

    def list_scene_files(self, scene_id: str)-> Dict[str, Any]:
        url = self._make_url(f"/v1/scene/{scene_id}/list")
        response = self.session.get(url)
        response.raise_for_status()
        return response.json()

    def upload_file(self, scene_id: str, file_path: str)-> Dict[str, Any]:
        url = self._make_url(f"/v1/scene/{scene_id}/upload")

        file_content = open(file_path, "rb")

        try:
            filename = os.path.basename(file_path)

            if filename.lower().endswith('.zip'):
                content_type = 'application/zip'
            elif filename.lower().endswith(('.jpg', '.jpeg')):
                content_type = 'image/jpeg'
            elif filename.lower().endswith('.png'):
                content_type = 'image/png'
            else:
                content_type = 'application/octet-stream'

            files = {
                'file': (filename, file_content, content_type)
            }

            response = self.session.post(url, files=files)

            if response.status_code >= 400:
                print(f"Upload failed with status code: {response.status_code}")
                print(f"Response content: {response.text}")
                print(f"Request details: URL={url}, filename={filename}, content_type={content_type}")

            response.raise_for_status()
            return response.json()
        finally:
            file_content.close()

    def get_file(self, scene_id: str, file_path: str) -> bytes:
        url = self._make_url(f"/v1/scene/{scene_id}/file")
        params = {'filepath': file_path}

        response = self.session.get(url, params=params)
        response.raise_for_status()
        return response.content

    def execute_scene(self, scene_id: str, nodes: List[Dict[str, Any]]) -> Dict[str, Any]:
        url = self._make_url(f"/v1/scene/{scene_id}/exec")
        payload = {"nodes": nodes}

        response = self.session.post(url, json=payload)
        response.raise_for_status()
        return response.json()


def create_node(node_id: int, node_type: str, inputs: Dict[str, Any]) -> Dict[str, Any]:
    return {
        "id": node_id,
        "type": node_type,
        "inputs": inputs
    }