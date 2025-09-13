from sdk import NodeFlow

flow = NodeFlow()

scene_id = flow.create_scene()

flow.upload_file("test_res/file.jpg")
flow.upload_file("test_res/scene_1_files.zip")

files = flow.list_files()

access_key_id = flow.String(input="key")
secret_access_key = flow.String(input="secret")
bucket = flow.String(input="bucket")
region = flow.String(input="region")

s3_input = flow.S3Input(
    access_key_id=access_key_id.output,
    secret_access_key=secret_access_key.output,
    bucket=bucket.output,
    region=region.output
)

input_node = flow.Input(input=files)
floor_result = flow.Floor(input=56)

combined = flow.Combine(
    files_0=input_node.output,
    files_1=s3_input.files
)

blurred = flow.GaussianBlur(
    input=combined.output,
    sigmaX=floor_result.output,
    sizeX=33,
    sizeY=33
)

output = flow.Output(
    input=blurred.output,
    prefix="output1",
    folder="output_1"
)

s3_output = flow.S3Output(
    input=blurred.output,
    access_key_id=access_key_id.output,
    secret_access_key=secret_access_key.output,
    bucket=bucket.output,
    region=region.output,
    folder="output"
)

execution_result = flow.execute()

flow.download_file("file.jpg", "test_res/downloaded_result.png")
print(flow.list_files())