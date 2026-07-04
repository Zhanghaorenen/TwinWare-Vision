package com.warehouse.twin.config;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.*;
import java.time.LocalDateTime;
@Configuration
public class MybatisConfig {
    @Bean public MybatisPlusInterceptor interceptor(){var i=new MybatisPlusInterceptor();i.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));return i;}
    @Bean public MetaObjectHandler metaObjectHandler(){return new MetaObjectHandler(){
        public void insertFill(MetaObject m){strictInsertFill(m,"createTime",LocalDateTime.class,LocalDateTime.now());strictInsertFill(m,"updateTime",LocalDateTime.class,LocalDateTime.now());}
        public void updateFill(MetaObject m){strictUpdateFill(m,"updateTime",LocalDateTime.class,LocalDateTime.now());}
    };}
}
