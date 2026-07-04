package com.warehouse.twin.config;
import com.warehouse.twin.common.Result;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class) public Result<Void> valid(MethodArgumentNotValidException e){return Result.fail(400,e.getBindingResult().getFieldErrors().get(0).getDefaultMessage());}
    @ExceptionHandler(DuplicateKeyException.class) public Result<Void> duplicate(DuplicateKeyException e){return Result.fail(409,"编号或唯一字段已存在");}
    @ExceptionHandler(IllegalArgumentException.class) public Result<Void> bad(IllegalArgumentException e){return Result.fail(400,e.getMessage());}
    @ExceptionHandler(Exception.class) public Result<Void> error(Exception e){return Result.fail(500,e.getMessage());}
}
