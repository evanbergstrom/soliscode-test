package org.soliscode.test.contract.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.breakable.BreakableCollection;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.contract.support.WithString;

/**
 * **Tests for the `SerializableContract` test suite**
 *
 * This class provides tests to ensure that the [SerializableContract] itself works as expected.
 * It uses various implementations to verify that the contract tests correctly identify
 * both compliant and non-compliant behaviors.
 *
 * ## Test Scope
 * The tests in this class cover:
 * - Basic [java.io.Serializable] contract compliance for standard classes (e.g., [String]).
 * - Verification of serialization and deserialization round-trips for [BreakableCollection].
 *
 * @author evanbergstrom
 * @see SerializableContract
 * @since 1.0.0
 */
@DisplayName("Tests for the SerializableContract")
public class SerializableContractTest {

    /**
     * **Test `SerializableContract` with the `String` class**
     *
     * This nested class verifies that [SerializableContract] correctly identifies [String]
     * as a class that complies with the standard [java.io.Serializable] contract.
     */
    @Nested
    @DisplayName("Test SerializableContract with String class")
    public class TestWithStringClass extends AbstractTest implements SerializableContract<String>, WithString {
    }

    /**
     * **Test `SerializableContract` with the `BreakableCollection` class**
     *
     * This nested class verifies that [SerializableContract] correctly identifies
     * [BreakableCollection] as a class that complies with the standard [java.io.Serializable] contract.
     */
    @Nested
    @DisplayName("Test SerializableContract with BreakableCollection class")
    public class TestWithBreakableCollection extends AbstractTest
            implements SerializableContract<BreakableCollection<Integer>>,
            BreakableCollection.WithProvider<Integer>, WithIntegerElement {
    }
}
