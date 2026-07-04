# 系统设计

## 边界与数据流

固定摄像头安装在墙面、顶部或货架前方。YOLOv5 只输出类别、置信度和 bbox，不判断取走/放置。Spring Boot 使用摄像头绑定的库位 polygon 和时间窗口形成业务事件。

```text
固定 RTSP 摄像头
  → FastAPI/OpenCV 抽帧
  → YOLOv5 目标检测
  → POST /api/detect/result
  → bbox/polygon 匹配
  → Redis/内存 8 帧窗口（6 帧多数确认）
  → EMPTY ↔ OCCUPIED 状态迁移
  → 事件、历史、告警写入 MySQL
  → WebSocket /ws/warehouse
  → Vue 状态页面 + Three.js 数字孪生
```

## 状态机

- 当前稳定态 `EMPTY`，窗口出现货物但未达到阈值：推送候选态 `PUTTING`，不改变库存。
- 8帧中至少6帧有货：稳定为 `OCCUPIED`；若原态为 `EMPTY`，生成 `PUT_IN`。
- 当前稳定态 `OCCUPIED`，窗口缺货但未达到阈值：推送候选态 `TAKING`。
- 8帧中至少6帧无货：稳定为 `EMPTY`；若原态为 `OCCUPIED`，生成 `TAKE_OUT`。
- 人工修正生成独立 `MANUAL_CORRECTION` 事件和历史，保证可追溯。

## 区域匹配

先判断 bbox 中心点是否位于 polygon；不满足时，以 polygon 外接矩形近似计算“bbox 被库位覆盖比例”，默认阈值为 0.35。凹多边形或精确 IoU 要求较高时，可将该实现替换为 JTS Polygon 运算，接口无需变化。

## Redis 与一致性

开发模式默认使用进程内滑动窗口，设置 `STATE_REDIS_ENABLED=true` 后同时将窗口写入 Redis。MySQL 是稳定状态和事件的事实来源。生产部署应将窗口读取也切换为 Redis Lua 原子脚本，并对同一 `cameraId` 做分区串行消费。

## 安全判断

- 当前闭环：人员与叉车 bbox 中心距离低于像素阈值时产生高风险告警。
- 多模态接口独立于货物状态机；风险分不低于0.75，或状态为疲劳/分心/高风险时产生人员告警。
- 取放执行者判定使用 YOLO 的 `person/hand/gripper/claw/robot_arm` 邻近证据，并可融合门控多模态模型提供的人员、机械爪动作分数。货物稳定消失后输出人员、机械爪、共同操作或未知。
- 像素距离不能代替真实距离。生产环境需通过固定相机单应性标定，将像素坐标映射到地面坐标。

## 生产化清单

加入 Spring Security/JWT 与角色权限；RTSP 凭据使用密钥服务；证据图存对象存储；MySQL/Redis 高可用；事件幂等键；消息队列削峰；相机心跳；日志审计；数据保留策略。告警、奖惩或劳动纪律必须由授权人员复核，模型不得自动执行工资扣罚。
