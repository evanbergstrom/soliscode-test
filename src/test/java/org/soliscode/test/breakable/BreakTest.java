package org.soliscode.test.breakable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/// Tests for [Break].
@DisplayName("Break")
public class BreakTest {

    /// Default constructor.
    public BreakTest() {
        super();
    }

    @Test
    @DisplayName("name() identifies the break, so that two breaks have different names")
    void name_whenCalled_identifiesTheBreak() {
        assertEquals(BreakableBlockingQueue.TAKE_THROWS_INTERRUPTED_EXCEPTION.description(),
                BreakableBlockingQueue.TAKE_THROWS_INTERRUPTED_EXCEPTION.name());
        assertNotEquals(BreakableBlockingQueue.TAKE_THROWS_INTERRUPTED_EXCEPTION.name(),
                BreakableBlockingQueue.PUT_THROWS_INTERRUPTED_EXCEPTION.name());
    }
}
