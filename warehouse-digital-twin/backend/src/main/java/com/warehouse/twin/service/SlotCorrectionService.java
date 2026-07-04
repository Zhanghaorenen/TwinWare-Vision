package com.warehouse.twin.service;
import com.warehouse.twin.dto.*; import com.warehouse.twin.entity.*; import com.warehouse.twin.mapper.*; import com.warehouse.twin.websocket.WarehouseWebSocketHandler; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.time.LocalDateTime;
@Service
public class SlotCorrectionService {
    private final SlotMapper slots;private final SlotStateHistoryMapper histories;private final WarehouseEventMapper events;private final CameraMapper cameras;private final WarehouseWebSocketHandler socket;
    public SlotCorrectionService(SlotMapper s,SlotStateHistoryMapper h,WarehouseEventMapper e,CameraMapper c,WarehouseWebSocketHandler w){slots=s;histories=h;events=e;cameras=c;socket=w;}
    @Transactional public Slot correct(Long id,SlotCorrectionRequest req){
        if(!java.util.Set.of("EMPTY","OCCUPIED","ABNORMAL").contains(req.status()))throw new IllegalArgumentException("人工修正状态只能是 EMPTY、OCCUPIED 或 ABNORMAL");Slot slot=slots.selectById(id);if(slot==null)throw new IllegalArgumentException("库位不存在");String old=slot.getStatus();slot.setStatus(req.status());slot.setCargoType(req.cargoType());slot.setLastUpdateTime(LocalDateTime.now());slots.updateById(slot);
        WarehouseEvent event=new WarehouseEvent();event.setEventType("MANUAL_CORRECTION");event.setCameraId(slot.getCameraId());event.setSlotId(id);event.setObjectType(req.cargoType());event.setEventTime(LocalDateTime.now());event.setStatus("CONFIRMED");event.setRemark(req.operator()+": "+(req.remark()==null?"":req.remark()));events.insert(event);
        SlotStateHistory h=new SlotStateHistory();h.setSlotId(id);h.setPreviousStatus(old);h.setCurrentStatus(req.status());h.setSource("MANUAL");h.setEventId(event.getId());h.setChangedAt(LocalDateTime.now());h.setRemark(req.operator());histories.insert(h);
        Camera c=cameras.selectById(slot.getCameraId());socket.broadcast(new TwinMessage("slot_update",slot.getCode(),slot.getStatus(),"MANUAL_CORRECTION",c==null?null:c.getCode(),LocalDateTime.now(),slot));return slot;
    }
}
