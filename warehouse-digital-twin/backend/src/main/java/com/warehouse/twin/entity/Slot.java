package com.warehouse.twin.entity;
import com.baomidou.mybatisplus.annotation.TableName; import lombok.*; import java.time.LocalDateTime;
@Data @EqualsAndHashCode(callSuper=true) @TableName("slot")
public class Slot extends BaseEntity { private Long warehouseId; private Long shelfId; private Long cameraId; private String code; private String polygon; private String status; private String cargoType; private LocalDateTime lastUpdateTime; private String remark; }
