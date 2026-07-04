package com.warehouse.twin.dto; import jakarta.validation.constraints.*;
public record SlotCorrectionRequest(@NotBlank String status,String cargoType,@NotBlank String operator,String remark){}
