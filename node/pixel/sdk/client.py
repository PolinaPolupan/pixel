import logging
import requests
from typing import List, Dict, Any, Optional
from urllib.parse import urljoin

from pixel.server.load_nodes import NODE_REGISTRY

logger = logging.getLogger(__name__)

class Client:
    engine_url = "http://engine:8080"
    session = requests.Session()

    @classmethod
    def _make_engine_url(cls, path: str) -> str:
        return urljoin(cls.engine_url, path)

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

    @classmethod
    def get_node_info(cls) -> Dict[str, Any]:
        result = {}
        for node_type, node_cls in NODE_REGISTRY.items():
            node = node_cls()
            result[node_type] = node.metadata
        return result

    @classmethod
    def create_graph(cls) -> str:
        url = cls._make_engine_url("/v1/graph/")
        response = cls.session.post(url)
        response.raise_for_status()
        return response.json().get("id")

    @classmethod
    def list_scene_files(cls, scene_id: str) -> Dict[str, Any]:
        url = cls._make_engine_url(f"/v1/scene/{scene_id}/list")
        response = cls.session.get(url)
        response.raise_for_status()
        return response.json()

    from typing import BinaryIO

    @classmethod
    def upload_file(cls, filename: str, file_obj: BinaryIO, content_type: Optional[str] = None) -> Dict[str, Any]:
        url = cls._make_engine_url(f"/v1/storage/upload")
        if not content_type:
            content_type = cls._guess_content_type(filename)

        files = {'file': (filename, file_obj, content_type)}
        response = cls.session.post(url, files=files)

        if response.status_code >= 400:
            logger.error(f"Upload failed: {response.status_code}, URL={url}, filename={filename}")
            logger.error(f"Response: {response.text}")
        response.raise_for_status()
        return response.json()

    @classmethod
    def get_file(cls, scene_id: str, file_path: str) -> bytes:
        url = cls._make_engine_url(f"/v1/scene/{scene_id}/file")
        params = {'filepath': file_path}
        response = cls.session.get(url, params=params)
        response.raise_for_status()
        return response.content

    @classmethod
    def execute_scene(cls, scene_id: str, nodes: List[Dict[str, Any]]) -> Dict[str, Any]:
        url = cls._make_engine_url(f"/v1/scene/{scene_id}/exec")
        payload = {"nodes": nodes}
        response = cls.session.post(url, json=payload)
        response.raise_for_status()
        return response.json()

    @classmethod
    def store_output(cls, source: str, folder: Optional[str] = None, prefix: Optional[str] = None) -> str:
        url = cls._make_engine_url("/v1/storage/output")
        params = {"source": source}
        if folder:
            params["folder"] = folder
        if prefix:
            params["prefix"] = prefix

        logger.info(f"Storing file source={source}, folder={folder}, prefix={prefix}")
        try:
            response = requests.post(url, params=params)
            response.raise_for_status()
            result_path = response.json()["path"]
            return result_path
        except requests.exceptions.RequestException as e:
            logger.error(f"Error storing file: {str(e)}")
            if getattr(e, "response", None):
                logger.error(f"Response status: {e.response.status_code}, body: {e.response.text}")
            raise

    @classmethod
    def store_task(cls, task_id: int, node_id: int, source: str) -> str:
        url = cls._make_engine_url("/v1/storage/task")
        params = {"taskId": task_id, "nodeId": node_id, "source": source}

        logger.info(f"Storing file to task: task_id={task_id}, node_id={node_id}, source={source}")
        try:
            response = requests.post(url, params=params)
            response.raise_for_status()
            result_path = response.json()["path"]
            return result_path
        except requests.exceptions.RequestException as e:
            logger.error(f"Error storing file to task: {str(e)}")
            if getattr(e, "response", None):
                logger.error(f"Response status: {e.response.status_code}, body: {e.response.text}")
            raise


def create_node(node_id: int, node_type: str, inputs: Dict[str, Any]) -> Dict[str, Any]:
    return {"id": node_id, "type": node_type, "inputs": inputs}
