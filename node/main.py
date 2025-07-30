from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from test_node import node_from_json

app = FastAPI()

@app.post("/validate")
async def validate(request: Request):
    try:
        data = await request.json()
        node = node_from_json(data)
        node.validate()
        return {"status": "ok"}
    except Exception as e:
        return JSONResponse(content={"status": "not ok", "error": str(e)}, status_code=400)

@app.post("/exec")
async def exec_node(request: Request):
    try:
        data = await request.json()
        node = node_from_json(data)
        outputs = node.exec()
        return outputs
    except Exception as e:
        return JSONResponse(content={"error": str(e)}, status_code=400)