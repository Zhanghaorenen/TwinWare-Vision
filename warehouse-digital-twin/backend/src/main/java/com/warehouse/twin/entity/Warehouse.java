package com.warehouse.twin.entity;
import com.baomidou.mybatisplus.annotation.TableName; import lombok.*;
@Data @EqualsAndHashCode(callSuper=true) @TableName("warehouse")
public class Warehouse extends BaseEntity { private String code; private String name; private String address; private String status; private String remark; }
