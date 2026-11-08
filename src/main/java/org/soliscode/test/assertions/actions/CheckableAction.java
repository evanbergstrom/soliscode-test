package org.soliscode.test.assertions.actions;

/// Interface for classes that are used to test methods that accept actions (*i.e.* suppliers, consumers, *etc.*).
///
/// @author evanbergstrom
/// @since 1.0
public interface CheckableAction {

    /// An assertion method that is called after a method had used the action and returned. This is used to test
    /// assertions on the aggregate effect of calling an action multiple times during the execution of a method.
    /// @throws org.opentest4j.AssertionFailedError if the assertion fails.
    void assertCheck();
}
