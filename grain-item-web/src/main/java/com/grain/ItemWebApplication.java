package com.grain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class ItemWebApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(ItemWebApplication.class, args);
        } catch (Exception e) {

        }
    }
}
