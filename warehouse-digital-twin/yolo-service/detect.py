from __future__ import annotations

import sys
from pathlib import Path

import cv2
import numpy as np
import torch

YOLO_ROOT = Path(__file__).resolve().parents[2]
if str(YOLO_ROOT) not in sys.path:
    sys.path.insert(0, str(YOLO_ROOT))


class YoloDetector:
    """YOLOv5 only reports what appears in a frame; it does not infer take/place events."""

    def __init__(self, weights: str, confidence=0.35, iou=0.45, image_size=640):
        path = Path(weights)
        if not path.is_absolute():
            path = (Path(__file__).parent / path).resolve()
        self.model = (
            torch.hub.load(str(YOLO_ROOT), "custom", path=str(path), source="local")
            if path.exists()
            else torch.hub.load(str(YOLO_ROOT), path.stem, source="local", pretrained=True)
        )
        self.model.conf, self.model.iou, self.image_size = confidence, iou, image_size

    def predict(self, frame: np.ndarray):
        result = self.model(frame, size=self.image_size)
        rows, objects = result.xyxy[0].cpu().numpy(), []
        for x1, y1, x2, y2, confidence, cls in rows:
            name = self.model.names[int(cls)]
            objects.append(
                {
                    "className": name,
                    "confidence": round(float(confidence), 4),
                    "bbox": [round(float(x1), 1), round(float(y1), 1), round(float(x2), 1), round(float(y2), 1)],
                }
            )
        annotated = np.asarray(result.render()[0])
        return objects, annotated

    def predict_bytes(self, content: bytes):
        frame = cv2.imdecode(np.frombuffer(content, np.uint8), cv2.IMREAD_COLOR)
        if frame is None:
            raise ValueError("无法解析图片")
        return self.predict(frame)
