package com.warehouse.twin.dto; import java.time.LocalDateTime;
public record TwinMessage(String type,String slotId,String status,String event,String cameraId,LocalDateTime time,Object data){}
