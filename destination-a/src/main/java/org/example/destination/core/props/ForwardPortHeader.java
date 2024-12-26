package org.example.destination.core.props;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ForwardPortHeader {
  private final ForwardPortPattern patterns;
  private final ForwardPort ports;

  public ForwardPortHeader(ForwardPortPattern patterns, ForwardPort ports) {
    this.patterns = patterns;
    this.ports = ports;
  }

}