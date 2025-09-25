package org.soliscode.test.provider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.soliscode.test.AbstractTest;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for the `StringProvider` class. These tests verify that the provider correctly implements
/// all inherited interface contracts and provides proper String instances for testing purposes.
///
/// @author evanbergstrom
/// @since 1.0
/// @see StringProvider
@DisplayName("Tests for StringProvider")
public class StringProviderTest extends AbstractTest {

    private final StringProvider provider = Providers.stringProvider();

    // Tests for ObjectProvider interface methods

    /// Test the defaultInstance method returns an empty string.
    @Test
    @DisplayName("Test defaultInstance method")
    public void testDefaultInstance() {
        String defaultValue = provider.defaultInstance();

        assertNotNull(defaultValue);
        assertEquals("", defaultValue);
        assertTrue(defaultValue.isEmpty());
    }

    /// Test the createInstance(long) method with various seed values.
    /// @param seed The primitive value to use as the seed value for the provided objects.
    @ParameterizedTest
    @ValueSource(longs = {0L, 1L, -1L, 42L, -42L, 1000L, -1000L, Long.MAX_VALUE, Long.MIN_VALUE})
    @DisplayName("Test createInstance(long) method with various seeds")
    public void testCreateInstanceWithSeed(long seed) {
        String result = provider.createInstance(seed);

        assertNotNull(result);
        assertEquals(String.valueOf(seed), result);
        assertEquals(Long.toString(seed), result);
    }

    /// Test the no-argument createInstance method defaults to seed 0.
    @Test
    @DisplayName("Test no-argument createInstance method")
    public void testCreateInstanceNoArgs() {
        String result = provider.createInstance();

        assertNotNull(result);
        assertEquals("0", result);
        assertEquals(String.valueOf(0L), result);
    }

    /// Test the copyInstance method with various string values.
    /// @param value The string value to use for testing copy behavior.
    @ParameterizedTest
    @ValueSource(strings = {"", "hello", "world", "test string", "123", "-456", "special chars: !@#$%^&*()"})
    @DisplayName("Test copyInstance method with various string values")
    public void testCopyInstance(String value) {
        String copy = provider.copyInstance(value);

        assertNotNull(copy);
        assertEquals(value, copy);
        // Test string immutability - should be the same reference
        assertSame(value, copy);
        assertEquals(value.hashCode(), copy.hashCode());
    }

    /// Test copyInstance with null input throws NullPointerException.
    @SuppressWarnings("DataFlowIssue") // explicitly testing passing a null parameter
    @Test
    @DisplayName("Test copyInstance method with null input")
    public void testCopyInstanceWithNull() {
        assertThrows(NullPointerException.class, () -> provider.copyInstance(null));
    }

    /// Test the uniqueSizeLimit method returns Long.MAX_VALUE.
    @Test
    @DisplayName("Test uniqueSizeLimit method")
    public void testUniqueSizeLimit() {
        long limit = provider.uniqueSizeLimit();
        assertEquals(Long.MAX_VALUE, limit);
    }

    // Tests for Supplier methods

    /// Test equalInstanceSupplier produces equal instances.
    @Test
    @DisplayName("Test equalInstanceSupplier method")
    public void testEqualInstanceSupplier() {
        Supplier<String> supplier = provider.equalInstanceSupplier();

        String first = supplier.get();
        String second = supplier.get();
        String third = supplier.get();

        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);

        assertEquals(first, second);
        assertEquals(second, third);
        assertEquals(first, third);

        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(second.hashCode(), third.hashCode());
    }

    /// Test uniqueInstanceSupplier produces unique instances.
    @Test
    @DisplayName("Test uniqueInstanceSupplier method")
    public void testUniqueInstanceSupplier() {
        Supplier<String> supplier = provider.uniqueInstanceSupplier();

        String first = supplier.get();   // seed 0
        String second = supplier.get();  // seed 1
        String third = supplier.get();   // seed 2

        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);

        assertNotEquals(first, second);
        assertNotEquals(second, third);
        assertNotEquals(first, third);

        assertEquals("0", first);
        assertEquals("1", second);
        assertEquals("2", third);
    }

    /// Test uniqueInstanceSupplier with custom seed.
    @Test
    @DisplayName("Test uniqueInstanceSupplier method with custom seed")
    public void testUniqueInstanceSupplierWithSeed() {
        Supplier<String> supplier = provider.uniqueInstanceSupplier(10L);

        String first = supplier.get();   // seed 10
        String second = supplier.get();  // seed 11
        String third = supplier.get();   // seed 12

        assertEquals("10", first);
        assertEquals("11", second);
        assertEquals("12", third);
    }

    /// Test randomInstanceSupplier produces valid instances.
    @Test
    @DisplayName("Test randomInstanceSupplier method")
    public void testRandomInstanceSupplier() {
        Supplier<String> supplier = provider.randomInstanceSupplier();

        String first = supplier.get();
        String second = supplier.get();
        String third = supplier.get();

        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);

        // Random instances should be valid string representations of numbers
        assertDoesNotThrow(() -> Long.parseLong(first));
        assertDoesNotThrow(() -> Long.parseLong(second));
        assertDoesNotThrow(() -> Long.parseLong(third));
    }

    // Tests for utility methods

    /// Test createEqualObjects method produces equal instances.
    /// @param size the size of the list of objects to create
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 10})
    @DisplayName("Test createEqualObjects method")
    public void testCreateEqualObjects(int size) {
        List<String> objects = provider.createEqualObjects(size);

        assertNotNull(objects);
        assertEquals(size, objects.size());

        if (size > 0) {
            String first = objects.getFirst();
            for (String obj : objects) {
                assertEquals(first, obj);
                assertEquals(first.hashCode(), obj.hashCode());
            }
        }
    }

    /// Test createUniqueInstances method produces unique instances.
    /// @param size the size of the list of objects to create
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 10})
    @DisplayName("Test createUniqueInstances method")
    public void testCreateUniqueInstances(int size) {
        List<String> instances = provider.createUniqueInstances(size);

        assertNotNull(instances);
        assertEquals(size, instances.size());

        // Check that all instances are unique
        for (int i = 0; i < instances.size(); i++) {
            for (int j = i + 1; j < instances.size(); j++) {
                assertNotEquals(instances.get(i), instances.get(j));
            }
        }

        // Check that values follow the expected pattern (seed-based)
        for (int i = 0; i < instances.size(); i++) {
            assertEquals(String.valueOf(i), instances.get(i));
        }
    }

    /// Test createUniqueInstances method with custom seed.
    @Test
    @DisplayName("Test createUniqueInstances method with custom seed")
    public void testCreateUniqueInstancesWithSeed() {
        int size = 5;
        long seed = 10L;
        List<String> instances = provider.createUniqueInstances(size, seed);

        assertNotNull(instances);
        assertEquals(size, instances.size());

        // Check that values follow the expected pattern starting from seed
        for (int i = 0; i < instances.size(); i++) {
            assertEquals(String.valueOf(seed + i), instances.get(i));
        }
    }

    /// Test createUniqueInstances throws exception when size exceeds limit.
    @Test
    @DisplayName("Test createUniqueInstances method with size exceeding limit")
    public void testCreateUniqueInstancesExceedsLimit() {
        // Create a mock provider with a smaller unique size limit for testing
        StringProvider limitedProvider = new StringProvider() {
            @Override
            public long uniqueSizeLimit() {
                return 5;
            }
        };

        assertThrows(IllegalArgumentException.class, () -> limitedProvider.createUniqueInstances(10));
    }

    /// Test createRandomInstances method produces valid instances.
    /// @param size the size of the list of objects to create
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 10})
    @DisplayName("Test createRandomInstances method")
    public void testCreateRandomInstances(int size) {
        List<String> instances = provider.createRandomInstances(size);

        assertNotNull(instances);
        assertEquals(size, instances.size());

        // All instances should be valid string representations of numbers
        for (String instance : instances) {
            assertNotNull(instance);
            assertDoesNotThrow(() -> Long.parseLong(instance));
        }
    }

    // Tests for consistency between related methods

    /// Test that defaultInstance and createInstance(0) produce equivalent results.
    @Test
    @DisplayName("Test consistency between defaultInstance and createInstance(0)")
    public void testDefaultInstanceConsistency() {
        String defaultValue = provider.defaultInstance();
        String zeroValue = provider.createInstance(0L);

        // These should NOT be equal - defaultInstance returns "" while createInstance(0) returns "0"
        assertNotEquals(defaultValue, zeroValue);
        assertEquals("", defaultValue);
        assertEquals("0", zeroValue);
    }

    /// Test that createInstance with same values produces equal results.
    @Test
    @DisplayName("Test consistency of createInstance with same values")
    public void testCreateInstanceConsistency() {
        String first = provider.createInstance(42L);
        String second = provider.createInstance(42L);

        assertEquals(first, second);
        assertEquals("42", first);
        assertEquals("42", second);
    }

    /// Test that copyInstance preserves the original value exactly.
    @Test
    @DisplayName("Test copyInstance preserves original value")
    public void testCopyInstanceConsistency() {
        String original = "test string";
        String copy = provider.copyInstance(original);

        assertEquals(original, copy);
        assertSame(original, copy); // String immutability
    }

    // Edge case tests

    /// Test behavior with extreme long values.
    @Test
    @DisplayName("Test edge cases with extreme long values")
    public void testExtremeLongValues() {
        // Test maximum long value
        String maxLong = provider.createInstance(Long.MAX_VALUE);
        assertNotNull(maxLong);
        assertEquals(String.valueOf(Long.MAX_VALUE), maxLong);
        assertEquals("9223372036854775807", maxLong);

        // Test minimum long value
        String minLong = provider.createInstance(Long.MIN_VALUE);
        assertNotNull(minLong);
        assertEquals(String.valueOf(Long.MIN_VALUE), minLong);
        assertEquals("-9223372036854775808", minLong);

        // Test zero
        String zero = provider.createInstance(0L);
        assertNotNull(zero);
        assertEquals("0", zero);

        // Test boundary values near zero
        String positiveOne = provider.createInstance(1L);
        String negativeOne = provider.createInstance(-1L);
        assertEquals("1", positiveOne);
        assertEquals("-1", negativeOne);
    }

    /// Test string properties and characteristics.
    @Test
    @DisplayName("Test string properties")
    public void testStringProperties() {
        // Test empty string
        String empty = provider.defaultInstance();
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.length());

        // Test positive number string
        String positive = provider.createInstance(123L);
        assertFalse(positive.isEmpty());
        assertEquals(3, positive.length());
        assertTrue(positive.matches("\\d+"));

        // Test negative number string
        String negative = provider.createInstance(-456L);
        assertFalse(negative.isEmpty());
        assertEquals(4, negative.length()); // includes the minus sign
        assertTrue(negative.matches("-\\d+"));
        assertTrue(negative.startsWith("-"));

        // Test zero string
        String zero = provider.createInstance(0L);
        assertEquals(1, zero.length());
        assertEquals("0", zero);
    }

    /// Test string immutability in copyInstance.
    @Test
    @DisplayName("Test string immutability behavior")
    public void testStringImmutability() {
        String original = "immutable test";
        String copy = provider.copyInstance(original);

        // Should be the exact same reference due to string immutability
        assertSame(original, copy);
        assertEquals(original, copy);

        // Test with different strings to ensure they're properly handled
        String first = provider.createInstance(100L);
        String second = provider.createInstance(100L);
        assertEquals(first, second);
        // Note: String.valueOf may or may not return the same reference for numbers

        String copied = provider.copyInstance(first);
        assertSame(first, copied);
    }

    /// Test that the provider follows all interface contracts properly.
    @Test
    @DisplayName("Test provider interface contracts")
    public void testProviderContracts() {
        // Test that the provider implements the expected interface
        assertInstanceOf(ObjectProvider.class, provider);

        // Test that generic type parameter is correctly specified
        ObjectProvider<String> objectProvider = provider;

        assertNotNull(objectProvider.defaultInstance());
        assertNotNull(objectProvider.createInstance(42L));
        assertNotNull(objectProvider.copyInstance("test"));
    }

    /// Test number parsing consistency.
    @Test
    @DisplayName("Test number parsing consistency")
    public void testNumberParsingConsistency() {
        // Test that created strings can be parsed back to the original numbers
        long[] testValues = {0L, 1L, -1L, 42L, -42L, 1000L, -1000L,
                            Long.MAX_VALUE, Long.MIN_VALUE};

        for (long value : testValues) {
            String stringValue = provider.createInstance(value);
            long parsedValue = Long.parseLong(stringValue);
            assertEquals(value, parsedValue);
        }
    }

    /// Test string content validation.
    @Test
    @DisplayName("Test string content validation")
    public void testStringContentValidation() {
        // Test that all created strings (except default) represent valid numbers
        for (int i = -10; i <= 10; i++) {
            String result = provider.createInstance(i);
            assertNotNull(result);
            assertFalse(result.isEmpty());

            // Should be parseable as a long
            long parsed = Long.parseLong(result);
            assertEquals(i, parsed);

            // Should not contain any whitespace or special characters (except minus for negatives)
            if (i >= 0) {
                assertTrue(result.matches("\\d+"));
            } else {
                assertTrue(result.matches("-\\d+"));
            }
        }
    }

    /// Test hash code and equality behavior.
    @Test
    @DisplayName("Test hash code and equality behavior")
    public void testHashCodeAndEquality() {
        // Test that equal strings have equal hash codes
        String value1 = provider.createInstance(100L);
        String value2 = provider.createInstance(100L);
        String copied = provider.copyInstance(value1);

        assertEquals(value1, value2);
        assertEquals(value1, copied);
        assertEquals(value1.hashCode(), value2.hashCode());
        assertEquals(value1.hashCode(), copied.hashCode());

        // Test that different values have different content
        String different = provider.createInstance(200L);
        assertNotEquals(value1, different);
        assertEquals("100", value1);
        assertEquals("200", different);
    }

    /// Test edge cases with string operations.
    @Test
    @DisplayName("Test string operation edge cases")
    public void testStringOperationEdgeCases() {
        // Test empty string behavior
        String empty = provider.defaultInstance();
        assertEquals("", empty);
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.length());

        // Test copy of empty string
        String emptyCopy = provider.copyInstance(empty);
        assertSame(empty, emptyCopy);
        assertEquals(empty, emptyCopy);

        // Test single character strings
        String zero = provider.createInstance(0L);
        assertEquals("0", zero);
        assertEquals(1, zero.length());

        // Test large number string
        String large = provider.createInstance(Long.MAX_VALUE);
        assertEquals("9223372036854775807", large);
        assertEquals(19, large.length());

        // Test negative number string
        String negative = provider.createInstance(Long.MIN_VALUE);
        assertEquals("-9223372036854775808", negative);
        assertEquals(20, negative.length()); // includes minus sign
    }
}