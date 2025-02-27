package org.example.destination.core.config;


import jakarta.servlet.DispatcherType;
import org.example.inbound.infrastructure.filter.ForwardedPortProcessingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class ServletFilterConfiguration {

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
