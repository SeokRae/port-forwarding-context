package org.example.destination;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@ComponentScan(basePackages = {"org.example.inbound", "org.example.destination"})
public class DestinationAApplication {

  public static void main(String[] args) {
    SpringApplication.run(DestinationAApplication.class, args);
  }

}
