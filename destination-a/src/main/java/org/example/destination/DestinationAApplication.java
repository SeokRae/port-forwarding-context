package org.example.destination;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DestinationAApplication {

  public static void main(String[] args) {
    SpringApplication.run(DestinationAApplication.class, args);
  }

}
