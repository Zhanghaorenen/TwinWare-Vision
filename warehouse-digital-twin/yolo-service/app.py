from __future__ import annotations
import threading
from datetime import datetime
from pathlib import Path
import cv2
import requests
import yaml
from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from fastapi.responses import Response
from pydantic import BaseModel
from detect import YoloDetector

ROOT = Path(__file__).parent
CONFIG = yaml.safe_load((ROOT / "config.yaml").read_text(encoding="utf-8"))
detector: YoloDetector | None = None
latest_frames: dict[str, bytes] = {}
workers: dict[str, tuple[threading.Thread, threading.Event]] = {}
interaction_cache: dict[str, dict] = {}
app = FastAPI(title="Warehouse YOLOv5 Service", version="1.0.0")

@app.on_event("startup")
def start_configured_streams():
    for item in CONFIG.get("cameras", []):
        if item.get("enabled"):
            start_stream(StreamRequest(cameraId=item["camera_id"], rtspUrl=item["rtsp_url"]))

def get_detector():
    global detector
    if detector is None:
        detector = YoloDetector(CONFIG["weights"], CONFIG["confidence"], CONFIG["iou"], CONFIG["image_size"])
    return detector

def publish(camera_id: str, objects: list, image_url: str = ""):
    payload = {"cameraId": camera_id, "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
               "imageUrl": image_url, "objects": objects, "interaction": interaction_cache.get(camera_id)}
    response = requests.post(CONFIG["backend_url"], json=payload, timeout=3)
    response.raise_for_status()
    return response.json()

class StreamRequest(BaseModel):
    cameraId: str
    rtspUrl: str

class FusionResult(BaseModel):
    cameraId: str
    personActionScore: float
    gripperActionScore: float
    fusionConfidence: float
    source: str = "gated-multimodal-fusion"

def stream_loop(camera_id: str, rtsp_url: str, stop: threading.Event):
    cap, frame_no = cv2.VideoCapture(rtsp_url), 0
    while not stop.is_set():
        ok, frame = cap.read()
        if not ok:
            stop.wait(1); cap.release(); cap = cv2.VideoCapture(rtsp_url); continue
        frame_no += 1
        if frame_no % int(CONFIG.get("process_every_n_frames", 3)): continue
        try:
            objects, annotated = get_detector().predict(frame)
            ok, encoded = cv2.imencode(".jpg", annotated)
            if ok: latest_frames[camera_id] = encoded.tobytes()
            publish(camera_id, objects, f"/latest/{camera_id}.jpg")
        except Exception as exc:
            print(f"[{camera_id}] detection failed: {exc}")
    cap.release()

@app.get("/health")
def health(): return {"status": "ok", "modelLoaded": detector is not None, "activeStreams": list(workers)}

@app.post("/fusion/result")
def fusion_result(body: FusionResult):
    values = (body.personActionScore, body.gripperActionScore, body.fusionConfidence)
    if any(value < 0 or value > 1 for value in values):
        raise HTTPException(400, "融合分数必须位于 0 到 1")
    interaction_cache[body.cameraId] = body.model_dump(exclude={"cameraId"})
    return {"message": "fusion result cached", "cameraId": body.cameraId,
            "interaction": interaction_cache[body.cameraId]}

@app.post("/detect/image")
async def detect_image(camera_id: str = Form(...), image: UploadFile = File(...)):
    try:
        objects, annotated = get_detector().predict_bytes(await image.read())
        ok, encoded = cv2.imencode(".jpg", annotated)
        if ok: latest_frames[camera_id] = encoded.tobytes()
        backend = publish(camera_id, objects, f"/latest/{camera_id}.jpg")
        return {"cameraId": camera_id, "objects": objects, "backend": backend}
    except Exception as exc: raise HTTPException(500, str(exc))

@app.post("/streams/start")
def start_stream(body: StreamRequest):
    if body.cameraId in workers: return {"message": "already running"}
    stop = threading.Event(); thread = threading.Thread(target=stream_loop, args=(body.cameraId, body.rtspUrl, stop), daemon=True);workers[body.cameraId] = (thread, stop);thread.start();return {"message": "started"}

@app.post("/streams/{camera_id}/stop")
def stop_stream(camera_id: str):
    worker = workers.pop(camera_id, None)
    if worker: worker[1].set()
    return {"message": "stopped"}

@app.get("/latest/{camera_id}.jpg")
def latest(camera_id: str):
    if camera_id not in latest_frames: raise HTTPException(404, "no frame")
    return Response(latest_frames[camera_id], media_type="image/jpeg")
