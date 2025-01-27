package org.example.source;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@ComponentScan(basePackages = {
  "org.example.inbound",
  "org.example.source",
  "org.example.client.rest",
})
public class SourceServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(SourceServerApplication.class, args);
  }

}
