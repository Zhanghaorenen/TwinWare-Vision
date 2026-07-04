# REST API

所有 REST 接口统一返回：`{"code":0,"message":"success","data":...,"timestamp":"..."}`。

| 模块 | 方法与路径 | 说明 |
|---|---|---|
| 仓库 | `GET/POST /api/warehouses`、`GET/PUT/DELETE /api/warehouses/{id}` | 仓库 CRUD |
| 区域 | `GET/POST /api/areas`、`GET/PUT/DELETE /api/areas/{id}` | 区域 CRUD |
| 货架 | `GET/POST /api/shelves`、`GET/PUT/DELETE /api/shelves/{id}` | 货架 CRUD |
| 摄像头 | `GET/POST /api/cameras`、`GET/PUT/DELETE /api/cameras/{id}` | 固定摄像头 CRUD |
| 库位 | `GET/POST /api/slots`、`GET/PUT/DELETE /api/slots/{id}` | 库位与 polygon 标定 |
| 库位 | `POST /api/slots/{id}/correct` | 人工修正并记录历史 |
| 检测 | `POST /api/detect/result` | 接收 YOLOv5 帧检测结果 |
| 检测 | `GET /api/detection-results` | 原始识别历史 |
| 事件 | `GET /api/events`、`GET /api/events/{id}` | 取放事件 |
| 事件 | `POST /api/events/{id}/review` | 人工复核，可生成奖惩记录 |
| 奖惩 | `GET /api/discipline-records` | 已复核的奖励/处罚审计记录 |
| 告警 | `GET /api/alarms` | 告警列表 |
| 告警 | `PUT /api/alarms/{id}/handle` | 处理告警 |
| 人员 | `POST/GET /api/person-state` | 多模态融合结果接口 |
| 孪生 | `GET /api/twin/warehouse-state` | 驾驶舱聚合 |
| 孪生 | `GET /api/twin/slot-state` | 全量库位状态 |
| 孪生 | `GET /api/twin/events` | 最近事件 |
| 实时 | `ws://host:8080/ws/warehouse` | 检测、候选态、状态、事件、告警推送 |

## YOLO 检测请求

```json
{
  "cameraId": "CAM_01",
  "timestamp": "2026-07-03 17:20:00",
  "imageUrl": "http://127.0.0.1:8000/latest/CAM_01.jpg",
  "objects": [
    {"className":"box","confidence":0.93,"bbox":[120,80,220,190]},
    {"className":"person","confidence":0.91,"bbox":[230,90,340,320]},
    {"className":"gripper","confidence":0.88,"bbox":[190,90,250,170]}
  ],
  "interaction": {
    "personActionScore": 0.84,
    "gripperActionScore": 0.90,
    "fusionConfidence": 0.86,
    "source": "gated-multimodal-fusion"
  }
}
```

`interaction` 是可选的多模态动作分数。后端会与 YOLO 连续8帧中的人员/机械爪邻近证据融合，事件的 `actorType` 输出为 `PERSON`、`GRIPPER`、`PERSON_AND_GRIPPER` 或 `UNKNOWN`。现有 `final_model2.py` 是情感分类训练脚本，需要使用仓库动作数据重新训练动作标签后才能提供这些分数。

重训后的融合推理服务可先调用 YOLO/FastAPI 的 `POST /fusion/result` 缓存当前摄像头动作分数；后续视频帧会自动把该结果随检测请求发送给 Spring Boot。

## 人工修正请求

```json
{"status":"OCCUPIED","cargoType":"carton","operator":"admin","remark":"现场盘点确认"}
```

## 人员状态请求

```json
{"personId":"P001","cameraId":"CAM_02","state":"FATIGUE","riskScore":0.82,
 "visualWeight":0.45,"audioWeight":0.25,"textWeight":0.10,"behaviorWeight":0.20,
 "environmentWeight":0,"timestamp":"2026-07-03T17:25:00"}
```
