package org.example.source.core.config;

import jakarta.servlet.DispatcherType;
import org.example.source.core.filter.PortProcessingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class ServletFilterConfiguration {

  @Bean
  public FilterRegistrationBean<PortProcessingFilter> portProcessingFilterRegistration(PortProcessingFilter portProcessingFilter) {
    FilterRegistrationBean<PortProcessingFilter> registrationBean = new FilterRegistrationBean<>();

    registrationBean.setFilter(portProcessingFilter);
    registrationBean.addUrlPatterns("/*");
    registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    registrationBean.setDispatcherTypes(DispatcherType.REQUEST);
    return registrationBean;
  }

}