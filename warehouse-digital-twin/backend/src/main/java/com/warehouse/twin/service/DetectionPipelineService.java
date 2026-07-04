package com.warehouse.twin.service;
import com.baomidou.mybatisplus.core.toolkit.Wrappers; import com.fasterxml.jackson.databind.ObjectMapper; import com.warehouse.twin.algorithm.*; import com.warehouse.twin.dto.*; import com.warehouse.twin.entity.*; import com.warehouse.twin.mapper.*; import com.warehouse.twin.websocket.WarehouseWebSocketHandler; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; import java.time.*; import java.util.*; import java.util.concurrent.ConcurrentHashMap;
@Service
public class DetectionPipelineService {
    private static final Set<String> CARGO=Set.of("box","carton","pallet","container");
    private static final Set<String> PERSON_ACTORS=Set.of("person","hand");
    private static final Set<String> GRIPPER_ACTORS=Set.of("gripper","claw","robot_arm","robot_gripper");
    private final CameraMapper cameras;private final SlotMapper slots;private final DetectionResultMapper results;private final WarehouseEventMapper events;private final SlotStateHistoryMapper histories;private final AlarmMapper alarms;private final PolygonMatcher matcher;private final StateWindowService windows;private final InteractionFusionService fusion;private final WarehouseWebSocketHandler socket;private final ObjectMapper json;private final Map<String,LocalDateTime> alarmCooldown=new ConcurrentHashMap<>();
    public DetectionPipelineService(CameraMapper c,SlotMapper s,DetectionResultMapper r,WarehouseEventMapper e,SlotStateHistoryMapper h,AlarmMapper a,PolygonMatcher m,StateWindowService w,InteractionFusionService f,WarehouseWebSocketHandler ws,ObjectMapper j){cameras=c;slots=s;results=r;events=e;histories=h;alarms=a;matcher=m;windows=w;fusion=f;socket=ws;json=j;}
    @Transactional
    public Map<String,Object> process(DetectionRequest req){
        Camera camera=cameras.selectOne(Wrappers.<Camera>lambdaQuery().eq(Camera::getCode,req.cameraId()));if(camera==null)throw new IllegalArgumentException("摄像头不存在: "+req.cameraId());
        LocalDateTime time=req.timestamp()==null?LocalDateTime.now():req.timestamp();DetectionResult saved=new DetectionResult();saved.setCameraId(camera.getId());saved.setCapturedAt(time);saved.setImageUrl(req.imageUrl());try{saved.setObjectsJson(json.writeValueAsString(req.objects()));}catch(Exception e){saved.setObjectsJson("[]");}results.insert(saved);
        List<DetectionRequest.DetectedObject> cargo=req.objects().stream().filter(o->CARGO.contains(o.className().toLowerCase())).toList();List<Map<String,Object>> changes=new ArrayList<>();
        for(Slot slot:slots.selectList(Wrappers.<Slot>lambdaQuery().eq(Slot::getCameraId,camera.getId()))){
            var matched=cargo.stream().filter(o->matcher.matches(o.bbox(),slot.getPolygon())).max(Comparator.comparingDouble(DetectionRequest.DetectedObject::confidence));boolean occupied=matched.isPresent();String old=slot.getStatus()==null?"EMPTY":slot.getStatus();
            double personConfidence=req.objects().stream().filter(o->PERSON_ACTORS.contains(o.className().toLowerCase())&&matcher.near(o.bbox(),slot.getPolygon(),160)).mapToDouble(DetectionRequest.DetectedObject::confidence).max().orElse(0);
            double gripperConfidence=req.objects().stream().filter(o->GRIPPER_ACTORS.contains(o.className().toLowerCase())&&matcher.near(o.bbox(),slot.getPolygon(),160)).mapToDouble(DetectionRequest.DetectedObject::confidence).max().orElse(0);
            InteractionFusionService.Decision actor=fusion.observe(slot.getId(),personConfidence,gripperConfidence,req.interaction());Optional<Boolean> stable=windows.append(slot.getId(),occupied);
            if(stable.isEmpty()){if(("EMPTY".equals(old)&&occupied)||("OCCUPIED".equals(old)&&!occupied)){String candidate=occupied?"PUTTING":"TAKING";socket.broadcast(new TwinMessage("slot_candidate",slot.getCode(),candidate,null,camera.getCode(),time,Map.of("evidence",windows.evidenceCount(slot.getId(),occupied),"actorType",actor.actorType(),"personScore",actor.personScore(),"gripperScore",actor.gripperScore())));}continue;}
            if(!Set.of("EMPTY","OCCUPIED").contains(old))continue;
            String target=stable.get()?"OCCUPIED":"EMPTY";if(!target.equals(old)){String type="EMPTY".equals(old)&&"OCCUPIED".equals(target)?"PUT_IN":"TAKE_OUT";boolean takeOut="TAKE_OUT".equals(type);double confidence=takeOut?Math.max(.75,actor.confidence()):matched.map(DetectionRequest.DetectedObject::confidence).orElse(0d);String objectType=matched.map(DetectionRequest.DetectedObject::className).orElse(slot.getCargoType());
                slot.setStatus(target);slot.setCargoType("EMPTY".equals(target)?null:objectType);slot.setLastUpdateTime(time);slots.updateById(slot);
                WarehouseEvent event=new WarehouseEvent();event.setEventType(type);event.setCameraId(camera.getId());event.setSlotId(slot.getId());event.setObjectType(objectType);event.setActorType(actor.actorType());event.setConfidence(BigDecimal.valueOf(confidence));event.setImageUrl(req.imageUrl());event.setEventTime(time);event.setStatus("PENDING");event.setRemark(actor.description(takeOut));events.insert(event);
                SlotStateHistory history=new SlotStateHistory();history.setSlotId(slot.getId());history.setPreviousStatus(old);history.setCurrentStatus(target);history.setSource("VISION");history.setEventId(event.getId());history.setChangedAt(time);histories.insert(history);
                TwinMessage message=new TwinMessage("slot_update",slot.getCode(),target,type,camera.getCode(),time,event);socket.broadcast(message);changes.add(Map.of("slotId",slot.getCode(),"status",target,"event",type,"actorType",actor.actorType()));
            }
        }
        detectSafety(req,camera,time);socket.broadcast(new TwinMessage("detection",null,null,null,camera.getCode(),time,req.objects()));return Map.of("detectionId",saved.getId(),"changedSlots",changes,"objectCount",req.objects().size());
    }
    private void detectSafety(DetectionRequest req,Camera camera,LocalDateTime time){
        List<DetectionRequest.DetectedObject> people=req.objects().stream().filter(x->x.className().equalsIgnoreCase("person")).toList();List<DetectionRequest.DetectedObject> forklifts=req.objects().stream().filter(x->x.className().equalsIgnoreCase("forklift")).toList();
        for(var p:people)for(var f:forklifts)if(distance(p.bbox(),f.bbox())<180){String key=camera.getCode()+":PERSON_FORKLIFT_PROXIMITY";LocalDateTime last=alarmCooldown.get(key);if(last!=null&&Duration.between(last,time).abs().getSeconds()<10)return;alarmCooldown.put(key,time);Alarm a=new Alarm();a.setAlarmType("PERSON_FORKLIFT_PROXIMITY");a.setLocation(camera.getInstallPosition());a.setLevel("HIGH");a.setTriggerTime(time);a.setImageUrl(req.imageUrl());a.setStatus("PENDING");a.setRemark("检测到人员与叉车距离过近");alarms.insert(a);socket.broadcast(new TwinMessage("alarm",null,null,"ALARM",camera.getCode(),time,a));return;}
    }
    private double distance(List<Double>a,List<Double>b){double ax=(a.get(0)+a.get(2))/2,ay=(a.get(1)+a.get(3))/2,bx=(b.get(0)+b.get(2))/2,by=(b.get(1)+b.get(3))/2;return Math.hypot(ax-bx,ay-by);}
}
