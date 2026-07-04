package com.warehouse.twin.entity;
import com.baomidou.mybatisplus.annotation.TableName; import lombok.*; import java.time.LocalDateTime;
@Data @EqualsAndHashCode(callSuper=true) @TableName("detection_result")
public class DetectionResult extends BaseEntity { private Long cameraId; private LocalDateTime capturedAt; private String objectsJson; private String imageUrl; }
