package org.soliscode.test;

/// Internal record that represents the support status of an optional method.
///
/// @param supported true if the method is supported, false if it is not supported
public record MethodStatus(boolean supported) {

    public static final MethodStatus SUPPORTED = new MethodStatus(true);

    public static final MethodStatus UNSUPPORTED = new MethodStatus(false);
}
