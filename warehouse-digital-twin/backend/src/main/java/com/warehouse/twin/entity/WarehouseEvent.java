package com.warehouse.twin.entity;
import com.baomidou.mybatisplus.annotation.TableName; import lombok.*; import java.math.BigDecimal; import java.time.LocalDateTime;
@Data @EqualsAndHashCode(callSuper=true) @TableName("warehouse_event")
public class WarehouseEvent extends BaseEntity { private String eventType; private Long cameraId; private Long slotId; private String objectType; private String actorType; private BigDecimal confidence; private String imageUrl; private LocalDateTime eventTime; private String status; private String remark; }
