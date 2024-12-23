package org.example.source.support.validator;

@FunctionalInterface
public interface PortValidator {
  boolean isValidPort(int port);
} 