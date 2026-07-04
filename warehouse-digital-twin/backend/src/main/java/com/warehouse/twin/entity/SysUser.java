package com.warehouse.twin.entity;
import com.baomidou.mybatisplus.annotation.TableName; import lombok.*;
@Data @EqualsAndHashCode(callSuper=true) @TableName("`user`")
public class SysUser extends BaseEntity { private String username; private String password; private String realName; private String role; private Integer status; }
