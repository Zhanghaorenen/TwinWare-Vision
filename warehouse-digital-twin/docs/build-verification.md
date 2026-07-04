# 构建验收记录

验收日期：2026-07-03（Asia/Shanghai）

## 已通过

- 后端：JDK 17 + Maven 3.9.16，执行 `mvn clean test`，`BUILD SUCCESS`。
- 后端测试：4 个测试全部通过，包括 polygon 匹配、8 帧中 6 帧稳定状态判断，以及人员与机械爪联合融合判定。
- 前端：Node.js 20.20.2，执行 `npm install` 和 `npm run build` 成功，2269 个模块完成转换。
- 前端依赖：npm audit 结果为 0 个已知漏洞。
- 识别服务：在 Conda `yolo5`（Python 3.10.20）中完成 AST 语法检查、模块导入和 FastAPI 健康函数检查。
- 识别依赖：PyTorch 2.12.1+cpu、TorchVision 0.27.1+cpu、OpenCV 5.0.0 及 YOLOv5 依赖可导入。

## 说明

- 当前源码目录没有 `.pt` 权重，因此没有执行真实图片推理。请将训练后的 `best.pt` 单独交付，并修改 `yolo-service/config.yaml`。
- MySQL/Redis/Docker 未在本次构建终端中启动，因此完成的是编译、单元测试和模块级验证，不包含真实 RTSP 与数据库端到端验收。
- Vite 提示生产 JS 包较大，这是 Three.js、ECharts、Element Plus 的性能优化提示，不影响构建成功。
