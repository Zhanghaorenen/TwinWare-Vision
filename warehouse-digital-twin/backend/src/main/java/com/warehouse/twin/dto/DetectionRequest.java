package com.warehouse.twin.dto;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.time.LocalDateTime; import java.util.List; import com.fasterxml.jackson.annotation.JsonFormat;
public record DetectionRequest(@NotBlank String cameraId,@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") LocalDateTime timestamp,String imageUrl,@NotNull @Valid List<DetectedObject> objects,@Valid InteractionContext interaction){
    public record DetectedObject(@NotBlank String className,@DecimalMin("0") @DecimalMax("1") double confidence,@NotNull @Size(min=4,max=4) List<Double> bbox){}
    public record InteractionContext(@DecimalMin("0") @DecimalMax("1") Double personActionScore,
                                     @DecimalMin("0") @DecimalMax("1") Double gripperActionScore,
                                     @DecimalMin("0") @DecimalMax("1") Double fusionConfidence,
                                     String source){}
}
