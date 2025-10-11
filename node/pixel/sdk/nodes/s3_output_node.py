import os
import boto3
import logging
from typing import List, Optional

from pixel.sdk.client import get_s3_credentials
from pixel.sdk.models.node_decorator import node
from pixel.core import Metadata

logger = logging.getLogger(__name__)


@node(
    inputs={
        "input": {"type": "FILEPATH_ARRAY", "required": True, "widget": "LABEL", "default": list()},
        "conn_id": {"type": "STRING", "required": True, "widget": "INPUT", "default": ""},
        "endpoint": {"type": "STRING", "required": False, "widget": "INPUT", "default": ""},
        "folder": {"type": "STRING", "required": False, "widget": "INPUT", "default": ""}
    },
    outputs={},
    display_name="S3 Output",
    category="IO",
    description="Output files to S3",
    color="#AED581",
    icon="OutputIcon"
)
def s3_output(
    input: List[str],
    conn_id: str,
    meta: Metadata,
    endpoint: Optional[str] = None,
    folder: str = ""
):
    creds = get_s3_credentials(conn_id)
    bucket = creds["bucket"]
    access_key_id = creds["access_key"]
    secret_access_key = creds["secret_key"]
    region = creds["region"]

    session = boto3.Session(
        aws_access_key_id=access_key_id,
        aws_secret_access_key=secret_access_key,
        region_name=region
    )

    s3_config = {}
    if endpoint and endpoint.strip():
        clean_endpoint = endpoint.strip().rstrip('/')
        s3_config['endpoint_url'] = clean_endpoint
        s3_config['use_ssl'] = clean_endpoint.startswith('https')
        s3_config['verify'] = False

    s3_client = session.client('s3', **s3_config)

    for file_path in input:
        if os.path.exists(file_path):
            filename = os.path.basename(file_path)
            key = f"{folder}/{filename}" if folder else filename
            with open(file_path, 'rb') as file_data:
                s3_client.upload_fileobj(file_data, bucket, key, ExtraArgs={'Metadata': {}})
            logger.info(f"Uploaded {file_path} to s3://{bucket}/{key}")

    return {}
