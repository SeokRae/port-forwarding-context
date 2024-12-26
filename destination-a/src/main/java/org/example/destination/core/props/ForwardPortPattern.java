package org.example.destination.core.props;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ForwardPortPattern {
  private final String suffix;
  private final String pattern;

  public ForwardPortPattern(String suffix, String pattern) {
    this.suffix = suffix;
    this.pattern = pattern;
  }
}
