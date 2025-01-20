package org.example.inbound.core.props;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@ToString
public abstract class AbstractForwardedPortProperties implements ForwardedPortConfigProperties {

  protected final PortHeader headers;

  protected AbstractForwardedPortProperties(PortHeader headers) {
    this.headers = headers;
    log.info("{}", this);
  }

  @Override
  public String getHeaderSuffix() {
    return headers.getPatterns().getSuffix();
  }

  @Override
  public String getHeaderPattern() {
    return headers.getPatterns().getPattern();
  }

  @Override
  public String getPortPattern() {
    return headers.getPorts().getPattern();
  }

  @Override
  public Integer getMinPort() {
    return headers.getPorts().getRange().getMin();
  }

  @Override
  public Integer getMaxPort() {
    return headers.getPorts().getRange().getMax();
  }

  @Getter
  @ToString
  public static class PortHeader {
    private final HeaderPattern patterns;

    private final Port ports;

    public PortHeader(HeaderPattern patterns, Port ports) {
      this.patterns = patterns;
      this.ports = ports;
    }

    @Getter
    @ToString
    public static class HeaderPattern {
      private final String suffix;

      private final String pattern;

      public HeaderPattern(String suffix, String pattern) {
        this.suffix = suffix;
        this.pattern = pattern;
      }
    }

    @Getter
    @ToString
    public static class Port {
      private final String pattern;
      private final PortRange range;

      public Port(String pattern, PortRange range) {
        this.pattern = pattern;
        this.range = range;
      }

      @Getter
      @ToString
      public static class PortRange {
        private final Integer min;
        private final Integer max;

        public PortRange(Integer min, Integer max) {
          validatePortRange(min, max);
          this.min = min;
          this.max = max;
        }

        private void validatePortRange(Integer min, Integer max) {
          if (min == null || max == null) {
            throw new IllegalArgumentException("Port range values cannot be null");
          }
          if (min < 1024 || min > 65535) {
            throw new IllegalArgumentException("Min port must be between 1024 and 65535");
          }
          if (max < 1024 || max > 65535) {
            throw new IllegalArgumentException("Max port must be between 1024 and 65535");
          }
          if (min > max) {
            throw new IllegalArgumentException("Min port cannot be greater than max port");
          }
        }
      }
    }
  }
}