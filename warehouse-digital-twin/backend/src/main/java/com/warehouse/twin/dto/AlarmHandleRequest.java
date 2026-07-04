package com.warehouse.twin.dto; import jakarta.validation.constraints.*;
public record AlarmHandleRequest(@NotBlank String handler,String remark){}
