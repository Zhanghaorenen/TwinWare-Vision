package com.warehouse.twin.controller;
import com.baomidou.mybatisplus.extension.service.IService; import com.warehouse.twin.common.Result; import org.springframework.web.bind.annotation.*; import java.io.Serializable; import java.util.List;
public abstract class BaseCrudController<T> {
    protected abstract IService<T> service();
    @GetMapping public Result<List<T>> list(){return Result.ok(service().list());}
    @GetMapping("/{id}") public Result<T> get(@PathVariable Serializable id){return Result.ok(service().getById(id));}
    @PostMapping public Result<T> create(@RequestBody T body){service().save(body);return Result.ok(body);}
    @PutMapping("/{id}") public Result<T> update(@PathVariable Long id,@RequestBody T body){try{body.getClass().getMethod("setId",Long.class).invoke(body,id);}catch(Exception e){throw new IllegalArgumentException("实体ID设置失败");}service().updateById(body);return Result.ok(body);}
    @DeleteMapping("/{id}") public Result<Boolean> delete(@PathVariable Serializable id){return Result.ok(service().removeById(id));}
}
