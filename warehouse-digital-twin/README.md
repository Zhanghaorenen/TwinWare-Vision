# 基于固定摄像头的智能仓储数字孪生底层支撑系统

项目包含 Spring Boot + MyBatis Plus 后端、Vue3 管理台、Three.js 数字孪生和 FastAPI + YOLOv5 识别服务。摄像头固定安装；YOLOv5 只检测目标，取放事件由后端状态机判断。

## 目录

```text
warehouse-digital-twin/
├── backend/       Spring Boot、MyBatis Plus、MySQL、Redis、WebSocket
├── frontend/      Vue3、Element Plus、ECharts、Three.js
├── yolo-service/  FastAPI、OpenCV、YOLOv5
├── docs/          SQL、API 与系统设计
└── docker-compose.yml
```

## 环境

- JDK 17+、Maven 3.6.3+
- Node.js 20.19+、npm 10+
- Python 3.10+（建议使用 YOLOv5 已配置好的虚拟环境）
- Docker Desktop（用于快速启动 MySQL/Redis，可选）

如果安装后旧终端仍识别到 Java 8，请重启 IDE/终端，或确认 `JAVA_HOME` 指向 JDK 17、`MAVEN_HOME` 和 Node/NVM 路径已加入 `PATH`。

## 1. 启动 MySQL 与 Redis

```powershell
cd warehouse-digital-twin
docker compose up -d
```

首次创建 MySQL 数据卷时会自动执行 `docs/database.sql`。如果已有旧数据卷，请手动执行 SQL，或在确认数据可删除后重建数据卷。

## 2. 启动后端

```powershell
cd backend
$env:MYSQL_PASSWORD="root"
mvn spring-boot:run
```

后端默认地址：`http://127.0.0.1:8080`。环境变量见 `backend/src/main/resources/application.yml`。

## 3. 启动前端

```powershell
cd frontend
npm install
npm run dev
```

访问 `http://127.0.0.1:5173`。页面包括驾驶舱、实时识别、库位、事件、告警、Three.js 数字孪生和人员状态。

## 4. 启动 YOLOv5 服务

在 YOLOv5 根目录的 Python 环境中：

```powershell
pip install -r warehouse-digital-twin/yolo-service/requirements.txt
cd warehouse-digital-twin/yolo-service
uvicorn app:app --host 0.0.0.0 --port 8000
```

编辑 `config.yaml` 设置权重、后端地址和固定 RTSP 摄像头。权重不存在时会按权重文件名尝试从本地 YOLOv5 Hub 模型入口加载预训练模型。生产检测类别必须使用仓库现场数据训练的自定义 `best.pt`。

测试单张图片：

```powershell
curl.exe -X POST http://127.0.0.1:8000/detect/image -F "camera_id=CAM_01" -F "image=@test.jpg"
```

启动一条固定 RTSP 流：

```powershell
Invoke-RestMethod -Method Post -Uri http://127.0.0.1:8000/streams/start -ContentType application/json -Body '{"cameraId":"CAM_01","rtspUrl":"rtsp://user:password@host/stream1"}'
```

## 验证

```powershell
cd backend
mvn test

cd ../frontend
npm run build
```

核心算法测试覆盖 polygon 匹配和“8帧中6帧”稳定判定。详细接口和状态设计见 `docs/`。
