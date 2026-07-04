package com.warehouse.twin.entity;
import com.baomidou.mybatisplus.annotation.TableName; import lombok.*;
@Data @EqualsAndHashCode(callSuper=true) @TableName("camera")
public class Camera extends BaseEntity { private String code; private String name; private String rtspUrl; private String installPosition; private Long areaId; private String monitorTarget; private Integer onlineStatus; private String remark; }
