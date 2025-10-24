package com.duplicate_filter.filter.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.duplicate_filter.filter.servlet_filter.filter.HashFilter;

@Configuration
public class FilterConfig {
    
    //Spring Container에 Bean으로 등록될 수 있도록 어노테이션 지정
    //Spring Container는 IoC를 통해 의존성을 효율적으로 관리할 수 있게 만들어줌
    //IoC는 객체의 생성과 의존성 주입을 개발자가 직접 하지 않고 프레임 워크(Spring Container)가 
    //대신 수행함으로써 제어 흐름이 역전되는 구조를 의미함.
     
    @Bean
    public FilterRegistrationBean<HashFilter> loggingHashFilter(){
        FilterRegistrationBean<HashFilter> registrationBean=new FilterRegistrationBean<>();

        registrationBean.setFilter(new HashFilter());
        //HashFilter 필터 클래스 적용 
        registrationBean.addUrlPatterns("/*");
        //모든 요청에 대해 필터링을 수행하도록 설정
        registrationBean.setOrder(0); 
        //최우선 순위로 필터링

        return registrationBean;
    }

}
