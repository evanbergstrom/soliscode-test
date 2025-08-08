package org.soliscode.test.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.object.ObjectContract;
import org.soliscode.test.contract.support.WithString;

@DisplayName("Tests for thhe ObjectContrct test")
public class ObjectContractTest {

    @Nested
    @DisplayName("Test ObjectContract with String class")
    public class TestWithStringClass extends AbstractTest implements ObjectContract<String>, WithString {
    }
}
