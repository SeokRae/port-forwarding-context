package org.example.destination.core.props;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Slf4j
@Getter
@ConfigurationProperties(prefix = "service.forwarding")
public class ForwardPortProperties {
  private final ForwardPortHeader headers;

  public ForwardPortProperties(ForwardPortHeader headers) {
    this.headers = headers;
  }

  @Getter
  @ToString
  public static class ForwardPortHeader {
    private final ForwardPortPattern patterns;
    private final ForwardPort ports;

    public ForwardPortHeader(ForwardPortPattern patterns, ForwardPort ports) {
      this.patterns = patterns;
      this.ports = ports;
    }

    @Getter
    @ToString
    public static class ForwardPortPattern {
      private final String suffix;
      private final String pattern;

      public ForwardPortPattern(String suffix, String pattern) {
        this.suffix = suffix;
        this.pattern = pattern;
      }

    }

    @Getter
    @ToString
    public static class ForwardPort {
      private final String pattern;
      private final ForwardPortRange range;

      public ForwardPort(String pattern, ForwardPortRange range) {
        this.pattern = pattern;
        this.range = range;
      }

      @Getter
      @ToString
      public static class ForwardPortRange {
        private final Integer min;
        private final Integer max;

        public ForwardPortRange(Integer min, Integer max) {
          this.min = min;
          this.max = max;
        }
      }
    }
  }
}
