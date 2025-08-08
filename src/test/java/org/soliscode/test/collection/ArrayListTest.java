package org.soliscode.test.collection;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.contract.DoesNotPermitIncompatibleTypes;
import org.soliscode.test.contract.DoesNotPermitNulls;
import org.soliscode.test.contract.list.ListContract;
import org.soliscode.test.contract.support.*;
import org.soliscode.test.provider.*;
import org.soliscode.test.util.CollectionTestOps;

import java.util.*;

@DisplayName("Test the TestList interface using an ArrayList")
public class ArrayListTest extends AbstractTest
        implements ListContract<Integer, ArrayList<Integer>>, WithArrayList<Integer>, WithIntegerElement {

    public ArrayListTest() {
    }

    @Nested
    @DisplayName("Test the TestList interface using an ArrayList that is unmodifiable")
    public class UnmodifiableArrayListTest extends AbstractTest
            implements ListContract<Integer, List<Integer>>, WithIntegerElement {

        public UnmodifiableArrayListTest() {
            doesNotSupportModification();
        }

        @Override
        public @NotNull CollectionProvider<Integer, List<Integer>> provider() {
            return CollectionProviders.wrap(CollectionProviders.provideArrayList(Providers.integerProvider()),
                    Collections::unmodifiableList);
        }
    }

    @Nested
    @DisplayName("Test the TestList interface using an ArrayList that does not accept nulls")
    public class NoNullsArrayListContract extends AbstractTest
            implements ListContract<Integer, List<Integer>>, WithIntegerElement, DoesNotPermitNulls {

        @Override
        public @NotNull CollectionProvider<Integer, List<Integer>> provider() {
            return CollectionProviders.wrap(CollectionProviders.provideArrayList(Providers.integerProvider()),
                    CollectionTestOps::preventNulls);
        }
    }

    @Nested
    @DisplayName("Test the TestList interface using an ArrayList that does not permit incompatible types")
    public class CheckedArrayListContract extends AbstractTest
            implements ListContract<Integer, List<Integer>>, WithIntegerElement, DoesNotPermitIncompatibleTypes {

        @Override
        public @NotNull CollectionProvider<Integer, List<Integer>> provider() {
            return CollectionProviders.wrap(CollectionProviders.provideArrayList(Providers.integerProvider()),
                    (c) -> Collections.checkedList(c, Integer.class));
        }
    }
}