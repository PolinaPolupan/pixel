import os
import boto3
from typing import Dict, Any
import logging

from pixel.models import Node

logger = logging.getLogger(__name__)

class S3OutputNode(Node):
    node_type = "S3Output"

    def get_input_types(self) -> Dict[str, Dict[str, Any]]:
        return {
            "input": {
                "type": "FILEPATH_ARRAY",
                "required": True,
                "widget": "LABEL",
                "default": set()
            },
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
            },
            "folder": {
                "type": "STRING",
                "required": False,
                "widget": "INPUT",
                "default": ""
            }
        }

    def get_output_types(self) -> Dict[str, Dict[str, Any]]:
        return {}

    def get_display_info(self) -> Dict[str, str]:
        return {
            "category": "IO",
            "description": "Output files to S3",
            "color": "#AED581",
            "icon": "OutputIcon"
        }

    def exec(self, input, access_key_id, secret_access_key, region, bucket, endpoint=None, folder="") -> Dict[str, Any]:
        session = boto3.Session(
            aws_access_key_id=access_key_id,
            aws_secret_access_key=secret_access_key,
            region_name=region
        )

        s3_config = {}
        if endpoint and endpoint.strip():
            s3_config['endpoint_url'] = endpoint
            s3_config['use_ssl'] = endpoint.startswith('https')
            s3_config['verify'] = False

        s3_client = session.client('s3', **s3_config)

        for file_path in input:
            if os.path.exists(file_path):
                filename = os.path.basename(file_path)
                key = f"{folder}/{filename}" if folder else filename

                with open(file_path, 'rb') as file_data:
                    s3_client.upload_fileobj(
                        file_data,
                        bucket,
                        key,
                        ExtraArgs={'Metadata': {}}
                    )

        return {}

    def validate(self, input, access_key_id, secret_access_key, region, bucket, endpoint=None) -> None:
        if not access_key_id:
            raise ValueError("Access key ID cannot be blank.")
        if not secret_access_key:
            raise ValueError("Secret cannot be blank.")
        if not region:
            raise ValueError("Region cannot be blank.")
        if not bucket:
            raise ValueError("Bucket cannot be blank.")