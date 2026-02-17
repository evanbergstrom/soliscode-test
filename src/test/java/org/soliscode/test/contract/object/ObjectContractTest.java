package org.soliscode.test.contract.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.support.WithString;

/**
 * **Tests for the `ObjectContract` test suite**
 *
 * This class provides tests to ensure that the [ObjectContract] itself works as expected.
 * It uses various implementations to verify that the contract tests correctly identify
 * both compliant and non-compliant behaviors.
 *
 * ## Test Scope
 * The tests in this class cover:
 * - Basic [Object] contract compliance for standard classes (e.g., [String]).
 * - Verification of [EqualsMethodContract], [HashCodeMethodContract], and [ToStringMethodContract].
 *
 * @author evanbergstrom
 * @see ObjectContract
 * @since 1.0.0
 */
@DisplayName("Tests for the ObjectContract test")
public class ObjectContractTest {

    /**
     * **Test `ObjectContract` with the `String` class**
     *
     * This nested class verifies that [ObjectContract] correctly identifies [String]
     * as a class that complies with the standard [Object] contract.
     */
    @Nested
    @DisplayName("Test ObjectContract with String class")
    public class TestWithStringClass extends AbstractTest implements ObjectContract<String>, WithString {
    }
}
