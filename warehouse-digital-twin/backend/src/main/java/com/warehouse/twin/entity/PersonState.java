package com.warehouse.twin.entity;
import com.baomidou.mybatisplus.annotation.TableName; import lombok.*; import java.math.BigDecimal; import java.time.LocalDateTime;
@Data @EqualsAndHashCode(callSuper=true) @TableName("person_state")
public class PersonState extends BaseEntity { private String personId; private String cameraId; private String state; private BigDecimal riskScore; private BigDecimal visualWeight; private BigDecimal audioWeight; private BigDecimal textWeight; private BigDecimal behaviorWeight; private BigDecimal environmentWeight; private LocalDateTime timestamp; }
