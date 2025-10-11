import boto3
import logging

from pixel.sdk.client import get_s3_credentials
from pixel.sdk.models.node_decorator import node
from pixel.core import Metadata
from pixel.sdk import Client

logger = logging.getLogger(__name__)

@node(
    inputs={"conn_id": {"type": "STRING", "required": True, "widget": "INPUT", "default": ""}},
    outputs={"files": {"type": "FILEPATH_ARRAY", "required": True}},
    display_name="S3 Input",
    category="IO",
    description="Load files from S3",
    color="#AED581",
    icon="S3Icon"
)
def s3_input(
    conn_id: str,
    meta: Metadata,
    endpoint: str = None
):
    creds = get_s3_credentials(conn_id)
    bucket = creds["bucket"]
    access_key = creds["access_key"]
    secret_key = creds["secret_key"]
    region = creds["region"]

    logger.info(f"S3 configuration - Region: {region}, Bucket: {bucket}")
    files = set()

    session = boto3.Session(
        aws_access_key_id=access_key,
        aws_secret_access_key=secret_key,
        region_name=region
    )

    s3_config = {}
    if endpoint and endpoint.strip():
        clean_endpoint = endpoint.strip().rstrip('/')
        s3_config['endpoint_url'] = clean_endpoint
        s3_config['use_ssl'] = clean_endpoint.startswith('https')

    try:
        s3_client = session.client('s3', **s3_config)
        response = s3_client.list_objects_v2(Bucket=bucket)

        if 'Contents' in response:
            logger.info(f"Found {len(response['Contents'])} objects in bucket")
            client = Client()
            for obj in response['Contents']:
                filename = obj['Key']
                file_response = s3_client.get_object(Bucket=bucket, Key=filename)
                body_stream = file_response['Body']

                upload_result = client.upload_file(filename=filename, file_obj=body_stream)
                logger.info(f"Uploaded {filename} -> {upload_result}")
                files.add(filename)
        else:
            logger.info("No files found in bucket")

    except Exception as e:
        logger.error(f"S3 error: {str(e)}")
        raise ValueError(f"Failed to connect to S3: {str(e)}")

    return {"files": files}
