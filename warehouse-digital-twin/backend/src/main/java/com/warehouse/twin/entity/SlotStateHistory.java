package com.warehouse.twin.entity;
import com.baomidou.mybatisplus.annotation.TableName; import lombok.*; import java.time.LocalDateTime;
@Data @EqualsAndHashCode(callSuper=true) @TableName("slot_state_history")
public class SlotStateHistory extends BaseEntity { private Long slotId; private String previousStatus; private String currentStatus; private String source; private Long eventId; private LocalDateTime changedAt; private String remark; }
