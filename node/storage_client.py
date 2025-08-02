import requests
from typing import Optional


class StorageClient:
    BASE_URL = "http://engine:8080/v1/storage"

    @staticmethod
    def store_from_workspace_to_scene(scene_id: int, source: str, folder: Optional[str] = None,
                                      prefix: Optional[str] = None) -> str:
        url = f"{StorageClient.BASE_URL}/workspace-to-scene"

        params = {
            "sceneId": scene_id,
            "source": source
        }

        if folder is not None:
            params["folder"] = folder

        if prefix is not None:
            params["prefix"] = prefix

        response = requests.post(url, params=params)
        response.raise_for_status()

        return response.json()["path"]

    @staticmethod
    def store_to_task(task_id: int, node_id: int, file_path: str, target: str) -> str:
        url = f"{StorageClient.BASE_URL}/to-task"

        params = {
            "taskId": task_id,
            "nodeId": node_id,
            "target": target
        }

        with open(file_path, 'rb') as f:
            files = {'file': f}
            response = requests.post(url, params=params, files=files)

        response.raise_for_status()

        return response.json()["path"]

    @staticmethod
    def store_from_workspace_to_task(task_id: int, node_id: int, source: str) -> str:
        url = f"{StorageClient.BASE_URL}/workspace-to-task"

        params = {
            "taskId": task_id,
            "nodeId": node_id,
            "source": source
        }

        response = requests.post(url, params=params)
        response.raise_for_status()

        return response.json()["path"]