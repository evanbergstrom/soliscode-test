package org.soliscode.test.contract.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.support.WithString;

@DisplayName("Tests for the ObjectContract test")
public class ObjectContractTest {

    @Nested
    @DisplayName("Test ObjectContract with String class")
    public class TestWithStringClass extends AbstractTest implements ObjectContract<String>, WithString {
    }
}
