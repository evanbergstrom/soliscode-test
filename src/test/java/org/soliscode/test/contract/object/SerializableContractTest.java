package org.soliscode.test.contract.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.support.WithString;

import org.jspecify.annotations.NonNull;
import org.soliscode.test.breakable.BreakableCollection;
import org.soliscode.test.contract.support.WithIntegerElement;
import org.soliscode.test.provider.CollectionProvider;

import java.util.Collection;

@DisplayName("Tests for the SerializableContract")
public class SerializableContractTest {

    @Nested
    @DisplayName("Test SerializableContract with String class")
    public class TestWithStringClass extends AbstractTest implements SerializableContract<String>, WithString {
    }

    @Nested
    @DisplayName("Test SerializableContract with BreakableCollection class")
    public class TestWithBreakableCollection extends AbstractTest
            implements SerializableContract<BreakableCollection<Integer>>,
            BreakableCollection.WithProvider<Integer>, WithIntegerElement {
    }
}
