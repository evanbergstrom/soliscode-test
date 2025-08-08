package org.soliscode.test.contract;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DynamicTest;
import org.opentest4j.AssertionFailedError;
import org.soliscode.test.breakable.Break;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public abstract class ContractTest<E,C extends Iterable<E>> {

    protected <X extends DynamicContract<E,C>> DynamicTest failingTest(String description, Break aBreak,
                                      Consumer<X> test) {
        return dynamicTest(description, () -> {
            assertThrows(AssertionFailedError.class,
                    () -> test.accept(createTest(aBreak)));
        });
    }

    protected abstract <X extends DynamicContract<E,C>> @NotNull X createTest(final Break b);
}
