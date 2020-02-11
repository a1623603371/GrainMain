package com.grain.config;

import com.grain.interceptors.Authlnterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {
    @Autowired
    Authlnterceptor authlnterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authlnterceptor).addPathPatterns("/**").excludePathPatterns("/error");
        super.addInterceptors(registry);
    }
}
