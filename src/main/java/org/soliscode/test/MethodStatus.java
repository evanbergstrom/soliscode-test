package org.soliscode.test;

/// Internal record that represents the support status of an optional method.
///
/// @param supported true if the method is supported, false if it is not supported
public record MethodStatus(boolean supported) {

    /// Represents a supported method status.
    public static final MethodStatus SUPPORTED = new MethodStatus(true);

    /// Represents an unsupported method status.
    public static final MethodStatus UNSUPPORTED = new MethodStatus(false);
}
