package com.richinfoai.server.config;


import com.richinfoai.server.utlis.BodyFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
public class ComponentFilterOrderConfig {
    @Bean
    public Filter MyHiddenHttpMethodFilter() {
        return new BodyFilter();//自定义的过滤器
    }


    @Bean
    public FilterRegistrationBean filterRegistrationBean1() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(MyHiddenHttpMethodFilter());
        filterRegistrationBean.addUrlPatterns("/api/realSecurity/saveProbeStatistics");
        filterRegistrationBean.setOrder(Integer.MIN_VALUE);//order的数值越小 则优先级越高
        return filterRegistrationBean;
    }

}