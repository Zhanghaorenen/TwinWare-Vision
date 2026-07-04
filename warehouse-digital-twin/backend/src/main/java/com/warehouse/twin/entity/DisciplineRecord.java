package com.warehouse.twin.entity;
import com.baomidou.mybatisplus.annotation.TableName; import lombok.*; import java.time.LocalDateTime;
@Data @EqualsAndHashCode(callSuper=true) @TableName("discipline_record")
public class DisciplineRecord extends BaseEntity {private Long eventId;private String personId;private String actionType;private Integer points;private String warningLevel;private String reason;private String reviewer;private LocalDateTime reviewedAt;}
