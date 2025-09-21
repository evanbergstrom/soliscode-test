package org.soliscode.test.provider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.soliscode.test.AbstractTest;
import org.soliscode.test.assertions.Assertions;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for the `LongProvider` class. These tests verify that the provider correctly implements
/// all inherited interface contracts and provides proper Integer instances for testing purposes.
///
/// @author evanbergstrom
/// @since 1.0
/// @see LongProvider
@DisplayName("Tests for IntegerProvider")
public class LongProviderTest extends AbstractTest {

    private final LongProvider provider = Providers.longProvider();

    // Tests for IntegerNumberProvider interface methods

    /// Test that the createValue method correctly creates Long instances from primitive long values.
    /// @param value The primitive value to use for the provided objects.
    @ParameterizedTest
    @ValueSource(longs = {0, 1, -1, 42, -42, 100, -100, Long.MAX_VALUE, Long.MIN_VALUE})
    @DisplayName("Test createValue method with various integer values")
    public void testCreateValue(long value) {
        Long result = provider.createValue(value);

        assertNotNull(result);
        assertEquals(value, result.longValue());
        assertEquals(Long.valueOf(value), result);
    }

    /// Test the maxIntegerValue method returns Integer.MAX_VALUE.
    @Test
    @DisplayName("Test maxIntegerValue method")
    public void testMaxIntegerValue() {
        long maxValue = provider.maxIntegerValue();
        assertEquals(Long.MAX_VALUE, maxValue);
    }

    /// Test the minIntegerValue method returns Long.MIN_VALUE.
    @Test
    @DisplayName("Test minIntegerValue method")
    public void testMinIntegerValue() {
        long minValue = provider.minIntegerValue();
        assertEquals(Long.MIN_VALUE, minValue);
    }

    // Tests for NumberProvider interface methods

    /// Test the maxValue method returns an Long wrapping Long.MAX_VALUE.
    @Test
    @DisplayName("Test maxValue method")
    public void testMaxValue() {
        Long maxValue = provider.maxValue();

        assertNotNull(maxValue);
        assertEquals(Long.MAX_VALUE, maxValue.longValue());
        assertEquals(Long.valueOf(Long.MAX_VALUE), maxValue);
    }

    /// Test the minValue method returns an Long wrapping Long.MIN_VALUE.
    @Test
    @DisplayName("Test minValue method")
    public void testMinValue() {
        Long minValue = provider.minValue();

        assertNotNull(minValue);
        assertEquals(Long.MIN_VALUE, minValue.longValue());
        assertEquals(Long.valueOf(Long.MIN_VALUE), minValue);
    }

    // Tests for ObjectProvider interface methods

    /// Test the defaultInstance method returns an Long with value 0.
    @Test
    @DisplayName("Test defaultInstance method")
    public void testDefaultInstance() {
        Long defaultValue = provider.defaultInstance();

        assertNotNull(defaultValue);
        assertEquals(0, defaultValue.longValue());
        assertEquals(Long.valueOf(0), defaultValue);
    }

    /// Test the createInstance(long) method with various seed values.
    /// @param seed The primitive value to use as the seed value for the provided objects.
    @ParameterizedTest
    @ValueSource(longs = {0, 1, -1, 42, -42, 1000, -1000, Long.MAX_VALUE, Long.MIN_VALUE})
    @DisplayName("Test createInstance(int) method with various seeds")
    public void testCreateInstanceWithSeed(long seed) {
        Long result = provider.createInstance(seed);

        assertNotNull(result);
        assertEquals(seed, result.longValue());
        assertEquals(Long.valueOf(seed), result);
    }

    /// Test the no-argument createInstance method defaults to seed 0.
    @Test
    @DisplayName("Test no-argument createInstance method")
    public void testCreateInstanceNoArgs() {
        Long result = provider.createInstance();

        assertNotNull(result);
        assertEquals(0, result.longValue());
        assertEquals(Long.valueOf(0), result);
    }

    /// Test the copyInstance method creates equal copies.
    /// @param value The primitive value to use for the provided objects.
    @ParameterizedTest
    @ValueSource(longs = {0, 1, -1, 42, -42, 1000, -1000, Long.MAX_VALUE, Long.MIN_VALUE})
    @DisplayName("Test copyInstance method with various values")
    public void testCopyInstance(long value) {
        Long original = value;
        Long copy = provider.copyInstance(original);

        assertNotNull(copy);
        assertEquals(original, copy);
        assertEquals(original.longValue(), copy.longValue());
        assertEquals(original.hashCode(), copy.hashCode());
    }

    /// Test copyInstance throws NullPointerException for null input.
    @SuppressWarnings("DataFlowIssue") // explicitly testing passing a null parameter
    @Test
    @DisplayName("Test copyInstance method with null input")
    public void testCopyInstanceWithNull() {
        assertThrows(NullPointerException.class, () -> {
            provider.copyInstance(null);
        });
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
        Supplier<Long> supplier = provider.equalInstanceSupplier();

        Long first = supplier.get();
        Long second = supplier.get();
        Long third = supplier.get();

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
        Supplier<Long> supplier = provider.uniqueInstanceSupplier();

        Long first = supplier.get();   // seed 0
        Long second = supplier.get();  // seed 1
        Long third = supplier.get();   // seed 2

        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);

        assertNotEquals(first, second);
        assertNotEquals(second, third);
        assertNotEquals(first, third);

        assertEquals(0, first.longValue());
        assertEquals(1, second.longValue());
        assertEquals(2, third.longValue());
    }

    /// Test uniqueInstanceSupplier with custom seed.
    @Test
    @DisplayName("Test uniqueInstanceSupplier method with custom seed")
    public void testUniqueInstanceSupplierWithSeed() {
        Supplier<Long> supplier = provider.uniqueInstanceSupplier(10);

        Long first = supplier.get();   // seed 10
        Long second = supplier.get();  // seed 11
        Long third = supplier.get();   // seed 12

        assertEquals(10, first.longValue());
        assertEquals(11, second.longValue());
        assertEquals(12, third.longValue());
    }

    /// Test randomInstanceSupplier produces valid instances.
    @Test
    @DisplayName("Test randomInstanceSupplier method")
    public void testRandomInstanceSupplier() {
        Supplier<Long> supplier = provider.randomInstanceSupplier();

        Long first = supplier.get();
        Long second = supplier.get();
        Long third = supplier.get();

        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);

        // Random instances should be valid Integer values within the valid range
        Assertions.assertGreaterThanOrEqual(first, Long.MIN_VALUE);
        Assertions.assertLessThanOrEqual(first, Long.MAX_VALUE);

        Assertions.assertGreaterThanOrEqual(second, Long.MIN_VALUE);
        Assertions.assertLessThanOrEqual(second, Long.MAX_VALUE);

        Assertions.assertGreaterThanOrEqual(third, Long.MIN_VALUE);
        Assertions.assertLessThanOrEqual(third, Long.MAX_VALUE);
    }

    // Tests for utility methods

    /// Test createEqualObjects method produces equal instances.
    /// @param size the size of the list of objects to create
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 10})
    @DisplayName("Test createEqualObjects method")
    public void testCreateEqualObjects(int size) {
        List<Long> objects = provider.createEqualObjects(size);

        assertNotNull(objects);
        assertEquals(size, objects.size());

        if (size > 0) {
            Long first = objects.getFirst();
            for (Long obj : objects) {
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
        List<Long> instances = provider.createUniqueInstances(size);

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
            assertEquals(i, instances.get(i).longValue());
        }
    }

    /// Test createUniqueInstances method with custom seed.
    @Test
    @DisplayName("Test createUniqueInstances method with custom seed")
    public void testCreateUniqueInstancesWithSeed() {
        int size = 5;
        int seed = 10;
        List<Long> instances = provider.createUniqueInstances(size, seed);

        assertNotNull(instances);
        assertEquals(size, instances.size());

        // Check that values follow the expected pattern starting from seed
        for (int i = 0; i < instances.size(); i++) {
            assertEquals(seed + i, instances.get(i).longValue());
        }
    }

    /// Test createUniqueInstances throws exception when size exceeds limit.
    @Test
    @DisplayName("Test createUniqueInstances method with size exceeding limit")
    public void testCreateUniqueInstancesExceedsLimit() {
        // Create a mock provider with a smaller unique size limit for testing
        LongProvider limitedProvider = new LongProvider() {
            @Override
            public long uniqueSizeLimit() {
                return 5;
            }
        };

        assertThrows(IllegalArgumentException.class, () -> {
            limitedProvider.createUniqueInstances(10);
        });
    }

    /// Test createRandoInstances method produces valid instances.
    /// @param size the size of the list of objects to create
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 10})
    @DisplayName("Test createRandoInstances method")
    public void testCreateRandoInstances(int size) {
        List<Long> instances = provider.createRandomInstances(size);

        assertNotNull(instances);
        assertEquals(size, instances.size());

        // All instances should be valid Integer values
        for (Long instance : instances) {
            assertNotNull(instance);
            Assertions.assertGreaterThanOrEqual(instance, Long.MIN_VALUE);
            Assertions.assertLessThanOrEqual(instance, Long.MAX_VALUE);
        }
    }

    // Tests for consistency between related methods

    /// Test that defaultInstance and createValue(0) produce equivalent results.
    @Test
    @DisplayName("Test consistency between defaultInstance and createValue(0)")
    public void testDefaultInstanceConsistency() {
        Long defaultValue = provider.defaultInstance();
        Long zeroValue = provider.createValue(0);

        assertEquals(defaultValue, zeroValue);
        assertEquals(defaultValue.intValue(), zeroValue.longValue());
    }

    /// Test that createInstance(0) and createValue(0) produce equivalent results.
    @Test
    @DisplayName("Test consistency between createInstance(0) and createValue(0)")
    public void testCreateInstanceConsistency() {
        Long fromSeed = provider.createInstance(0);
        Long fromValue = provider.createValue(0);

        assertEquals(fromSeed, fromValue);
        assertEquals(fromSeed.intValue(), fromValue.longValue());
    }

    /// Test that maxValue and createValue(maxIntegerValue()) produce equivalent results.
    @Test
    @DisplayName("Test consistency between maxValue and createValue(maxIntegerValue())")
    public void testMaxValueConsistency() {
        Long maxFromMethod = provider.maxValue();
        Long maxFromPrimitive = provider.createValue(provider.maxIntegerValue());

        assertEquals(maxFromMethod, maxFromPrimitive);
        assertEquals(maxFromMethod.longValue(), maxFromPrimitive.longValue());
    }

    /// Test that minValue and createValue(minIntegerValue()) produce equivalent results.
    @Test
    @DisplayName("Test consistency between minValue and createValue(minIntegerValue())")
    public void testMinValueConsistency() {
        Long minFromMethod = provider.minValue();
        Long minFromPrimitive = provider.createValue(provider.minIntegerValue());

        assertEquals(minFromMethod, minFromPrimitive);
        assertEquals(minFromMethod.longValue(), minFromPrimitive.longValue());
    }

    // Edge case tests

    /// Test behavior with extreme values.
    @Test
    @DisplayName("Test edge cases with extreme long values")
    public void testExtremeIntegerValues() {
        // Test maximum value
        Long maxInt = provider.createValue(Long.MAX_VALUE);
        assertNotNull(maxInt);
        assertEquals(Long.MAX_VALUE, maxInt.longValue());
        assertEquals(0x7fffffffffffffffL, maxInt.longValue());

        // Test minimum value
        Long minInt = provider.createValue(Long.MIN_VALUE);
        assertNotNull(minInt);
        assertEquals(Long.MIN_VALUE, minInt.longValue());
        assertEquals(0x8000000000000000L, minInt.longValue());

        // Test zero
        Long zero = provider.createValue(0);
        assertNotNull(zero);
        assertEquals(0, zero.longValue());

        // Test boundary values near zero
        Long positiveOne = provider.createValue(1);
        Long negativeOne = provider.createValue(-1);
        assertEquals(1, positiveOne.longValue());
        assertEquals(-1, negativeOne.longValue());
    }

    /// Test integer overflow scenarios at boundaries.
    @Test
    @DisplayName("Test integer boundary behavior")
    public void testIntegerBoundaries() {
        // Test that values are preserved correctly at boundaries
        Long maxValue = provider.createValue(Long.MAX_VALUE);
        Long minValue = provider.createValue(Long.MIN_VALUE);

        // Verify the values are exactly what we expect
        assertEquals(Long.MAX_VALUE, maxValue.longValue());
        assertEquals(Long.MIN_VALUE, minValue.longValue());

        // Test that copyInstance preserves boundary values
        Long copiedMax = provider.copyInstance(maxValue);
        Long copiedMin = provider.copyInstance(minValue);

        assertEquals(maxValue, copiedMax);
        assertEquals(minValue, copiedMin);
        assertEquals(maxValue.longValue(), copiedMax.longValue());
        assertEquals(minValue.longValue(), copiedMin.longValue());
    }

    /// Test that the provider follows all interface contracts properly.
    @SuppressWarnings("UnnecessaryLocalVariable")
    @Test
    @DisplayName("Test provider interface contracts")
    public void testProviderContracts() {
        // Test that the provider implements all expected interfaces
        assertInstanceOf(IntegerNumberProvider.class, provider);
        assertInstanceOf(NumberProvider.class, provider);
        assertInstanceOf(ObjectProvider.class, provider);

        // Test that generic type parameter is correctly specified
        ObjectProvider<Long> objectProvider = provider;
        NumberProvider<Long> numberProvider = provider;
        IntegerNumberProvider<Long> integerNumberProvider = provider;

        assertNotNull(objectProvider.defaultInstance());
        assertNotNull(numberProvider.maxValue());
        assertNotNull(integerNumberProvider.createValue(1));
    }

    // Tests for number conversion behavior

    /// Test that Integer instances work correctly with Number interface methods.
    @Test
    @DisplayName("Test Number interface conversion methods")
    public void testNumberConversions() {
        Long instance = provider.createValue(42);

        // Test all Number conversion methods
        assertEquals(42, instance.intValue());
        assertEquals(42L, instance.longValue());
        assertEquals(42.0f, instance.floatValue(), 0.0f);
        assertEquals(42.0, instance.doubleValue(), 0.0);
        assertEquals((byte) 42, instance.byteValue());
        assertEquals((short) 42, instance.shortValue());
    }

    /// Test hash code consistency for Integer values.
    @Test
    @DisplayName("Test hash code consistency")
    public void testHashCodeConsistency() {
        // Test that equal instances have equal hash codes
        Long value1 = provider.createValue(100);
        Long value2 = provider.createValue(100);
        Long copied = provider.copyInstance(value1);

        assertEquals(value1, value2);
        assertEquals(value1, copied);
        assertEquals(value1.hashCode(), value2.hashCode());
        assertEquals(value1.hashCode(), copied.hashCode());

        // Test that different values have different hash codes (usually)
        Long different = provider.createValue(200);
        assertNotEquals(value1, different);
        // Note: hash codes can collide, so we don't assert inequality
    }
}