package com.warehouse.twin.entity;
import com.baomidou.mybatisplus.annotation.TableName; import lombok.*;
@Data @EqualsAndHashCode(callSuper=true) @TableName("warehouse_area")
public class WarehouseArea extends BaseEntity { private Long warehouseId; private String code; private String name; private String polygon; private String remark; }
