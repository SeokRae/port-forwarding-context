package org.example.destination.core.props;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ForwardPort {
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