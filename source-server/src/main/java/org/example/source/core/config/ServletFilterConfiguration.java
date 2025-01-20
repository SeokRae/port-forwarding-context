package org.example.source.core.config;

import jakarta.servlet.DispatcherType;
import org.example.inbound.core.filter.ForwardedPortProcessingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class ServletFilterConfiguration {

  // Inbound 요청에 대한 공통 플랫폼 모듈의 ForwardedPortProcessingFilter를 등록한다.
  @Bean
  public FilterRegistrationBean<ForwardedPortProcessingFilter> portProcessingFilterRegistration(
    ForwardedPortProcessingFilter forwardedPortProcessingFilter
  ) {
    FilterRegistrationBean<ForwardedPortProcessingFilter> registrationBean = new FilterRegistrationBean<>();

    registrationBean.setFilter(forwardedPortProcessingFilter);
    registrationBean.addUrlPatterns("/*");
    registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    registrationBean.setDispatcherTypes(DispatcherType.REQUEST);
    return registrationBean;
  }

}
