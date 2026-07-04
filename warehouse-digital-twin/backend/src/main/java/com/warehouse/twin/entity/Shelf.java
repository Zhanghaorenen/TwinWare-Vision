package com.warehouse.twin.entity;
import com.baomidou.mybatisplus.annotation.TableName; import lombok.*;
@Data @EqualsAndHashCode(callSuper=true) @TableName("shelf")
public class Shelf extends BaseEntity { private Long areaId; private String code; private String name; private Integer rowCount; private Integer columnCount; private String remark; }
