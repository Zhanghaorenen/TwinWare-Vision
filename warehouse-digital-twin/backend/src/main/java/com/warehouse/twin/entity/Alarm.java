package com.warehouse.twin.entity;
import com.baomidou.mybatisplus.annotation.TableName; import lombok.*; import java.time.LocalDateTime;
@Data @EqualsAndHashCode(callSuper=true) @TableName("alarm")
public class Alarm extends BaseEntity { private String alarmType; private String location; private String level; private LocalDateTime triggerTime; private String imageUrl; private String status; private String handler; private LocalDateTime handledAt; private String remark; }
