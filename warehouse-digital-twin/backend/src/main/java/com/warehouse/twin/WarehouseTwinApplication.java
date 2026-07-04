package com.warehouse.twin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
@SpringBootApplication @MapperScan("com.warehouse.twin.mapper")
public class WarehouseTwinApplication { public static void main(String[] args){SpringApplication.run(WarehouseTwinApplication.class,args);} }
