package com.warehouse.twin.dto; import jakarta.validation.constraints.*;
public record EventReviewRequest(@NotBlank String decision,String personId,Integer points,String warningLevel,@NotBlank String reviewer,String remark){}
