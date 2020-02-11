package com.grain;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.grain.manage.mapper")
public class ManageApplication {
    public static void main(String[] args) {
        SpringApplication.run(ManageApplication.class, args);
    }

}
