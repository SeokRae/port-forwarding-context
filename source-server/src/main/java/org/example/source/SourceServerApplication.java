package org.example.source;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SourceServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(SourceServerApplication.class, args);
  }

}
