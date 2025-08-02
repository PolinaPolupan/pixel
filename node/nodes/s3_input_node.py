import os
import boto3
from typing import Dict, Any
import logging

from node import Node

logger = logging.getLogger(__name__)


class S3InputNode(Node):
    node_type = "S3Input"

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "access_key_id": {
                "type": "STRING",
                "required": True,
                "widget": "INPUT",
                "default": ""
            },
            "secret_access_key": {
                "type": "STRING",
                "required": True,
                "widget": "INPUT",
                "default": ""
            },
            "region": {
                "type": "STRING",
                "required": True,
                "widget": "INPUT",
                "default": ""
            },
            "bucket": {
                "type": "STRING",
                "required": True,
                "widget": "INPUT",
                "default": ""
            },
            "endpoint": {
                "type": "STRING",
                "required": False,
                "widget": "INPUT",
                "default": ""
            }
        }

    def get_output_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "files": {
                "type": "FILEPATH_ARRAY",
                "required": True
            }
        }

    def get_display_info(self) -> Dict[str, str]:
        return {
            "category": "IO",
            "description": "Load files from S3",
            "color": "#AED581",
            "icon": "S3Icon"
        }

    def exec(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        access_key = inputs.get("access_key_id", "")
        secret_key = inputs.get("secret_access_key", "")
        region_name = inputs.get("region", "")
        bucket = inputs.get("bucket", "")
        endpoint = inputs.get("endpoint", "")

        files = set()

        session = boto3.Session(
            aws_access_key_id=access_key,
            aws_secret_access_key=secret_key,
            region_name=region_name
        )

        s3_config = {}
        if endpoint and endpoint.strip():
            s3_config['endpoint_url'] = endpoint
            s3_config['use_ssl'] = endpoint.startswith('https')
            s3_config['verify'] = False  # For testing only

        s3_client = session.client('s3', **s3_config)

        response = s3_client.list_objects_v2(Bucket=bucket)

        if 'Contents' in response:
            for obj in response['Contents']:
                filename = obj['Key']
                logger.debug(f"Loading file from S3: {filename}")

                response = s3_client.get_object(Bucket=bucket, Key=filename)
                content = response['Body'].read()

                from storage_client import StorageClient

                task_id = inputs.get("meta", {}).get("taskId")
                node_id = inputs.get("meta", {}).get("nodeId")

                if task_id and node_id:
                    temp_file_path = f"/tmp/{filename}"
                    with open(temp_file_path, 'wb') as f:
                        f.write(content)

                    file_path = StorageClient.store_to_task(
                        task_id=task_id,
                        node_id=node_id,
                        file_path=temp_file_path,
                        target=filename
                    )

                    files.add(file_path)

                    os.remove(temp_file_path)

        task_id = inputs.get("meta", {}).get("taskId")
        node_id = inputs.get("meta", {}).get("nodeId")

        output_files = []

        for file in files:
            output_files.append(StorageClient.store_from_workspace_to_task(task_id, node_id, file))

        return {"files": output_files}

    def validate(self, inputs: Dict[str, Any]) -> None:
        if not inputs.get("access_key_id", ""):
            raise ValueError("Access key ID cannot be blank.")
        if not inputs.get("secret_access_key", ""):
            raise ValueError("Secret cannot be blank.")
        if not inputs.get("region", ""):
            raise ValueError("Region cannot be blank.")
        if not inputs.get("bucket", ""):
            raise ValueError("Bucket cannot be blank.")