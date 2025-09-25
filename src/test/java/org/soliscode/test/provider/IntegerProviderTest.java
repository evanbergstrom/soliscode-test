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

/// Tests for the `IntegerProvider` class. These tests verify that the provider correctly implements
/// all inherited interface contracts and provides proper Integer instances for testing purposes.
///
/// @author evanbergstrom
/// @since 1.0
/// @see IntegerProvider
@DisplayName("Tests for IntegerProvider")
public class IntegerProviderTest extends AbstractTest {

    private final IntegerProvider provider = Providers.integerProvider();

    // Tests for IntegerNumberProvider interface methods

    /// Test that the createValue method correctly creates Integer instances from primitive int values.
    /// @param value The primitive value to use for the provided objects.
    @ParameterizedTest
    @ValueSource(ints = {0, 1, -1, 42, -42, 100, -100, Integer.MAX_VALUE, Integer.MIN_VALUE})
    @DisplayName("Test createValue method with various integer values")
    public void testCreateValue(int value) {
        Integer result = provider.createValue(value);

        assertNotNull(result);
        assertEquals(value, result.intValue());
        assertEquals(Integer.valueOf(value), result);
    }

    /// Test the maxIntegerValue method returns Integer.MAX_VALUE.
    @Test
    @DisplayName("Test maxIntegerValue method")
    public void testMaxIntegerValue() {
        long maxValue = provider.maxIntegerValue();
        assertEquals(Integer.MAX_VALUE, maxValue);
    }

    /// Test the minIntegerValue method returns Integer.MIN_VALUE.
    @Test
    @DisplayName("Test minIntegerValue method")
    public void testMinIntegerValue() {
        long minValue = provider.minIntegerValue();
        assertEquals(Integer.MIN_VALUE, minValue);
    }

    // Tests for NumberProvider interface methods

    /// Test the maxValue method returns an Integer wrapping Integer.MAX_VALUE.
    @Test
    @DisplayName("Test maxValue method")
    public void testMaxValue() {
        Integer maxValue = provider.maxValue();

        assertNotNull(maxValue);
        assertEquals(Integer.MAX_VALUE, maxValue.intValue());
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), maxValue);
    }

    /// Test the minValue method returns an Integer wrapping Integer.MIN_VALUE.
    @Test
    @DisplayName("Test minValue method")
    public void testMinValue() {
        Integer minValue = provider.minValue();

        assertNotNull(minValue);
        assertEquals(Integer.MIN_VALUE, minValue.intValue());
        assertEquals(Integer.valueOf(Integer.MIN_VALUE), minValue);
    }

    // Tests for ObjectProvider interface methods

    /// Test the defaultInstance method returns an Integer with value 0.
    @Test
    @DisplayName("Test defaultInstance method")
    public void testDefaultInstance() {
        Integer defaultValue = provider.defaultInstance();

        assertNotNull(defaultValue);
        assertEquals(0, defaultValue.intValue());
        assertEquals(Integer.valueOf(0), defaultValue);
    }

    /// Test the createInstance(int) method with various seed values.
    /// @param seed The primitive value to use as the seed value for the provided objects.
    @ParameterizedTest
    @ValueSource(ints = {0, 1, -1, 42, -42, 1000, -1000, Integer.MAX_VALUE, Integer.MIN_VALUE})
    @DisplayName("Test createInstance(int) method with various seeds")
    public void testCreateInstanceWithSeed(int seed) {
        Integer result = provider.createInstance(seed);

        assertNotNull(result);
        assertEquals(seed, result.intValue());
        assertEquals(Integer.valueOf(seed), result);
    }

    /// Test the no-argument createInstance method defaults to seed 0.
    @Test
    @DisplayName("Test no-argument createInstance method")
    public void testCreateInstanceNoArgs() {
        Integer result = provider.createInstance();

        assertNotNull(result);
        assertEquals(0, result.intValue());
        assertEquals(Integer.valueOf(0), result);
    }

    /// Test the copyInstance method creates equal copies.
    /// @param value The primitive value to use for the provided objects.
    @ParameterizedTest
    @ValueSource(ints = {0, 1, -1, 42, -42, 1000, -1000, Integer.MAX_VALUE, Integer.MIN_VALUE})
    @DisplayName("Test copyInstance method with various values")
    public void testCopyInstance(int value) {
        Integer original = value;
        Integer copy = provider.copyInstance(original);

        assertNotNull(copy);
        assertEquals(original, copy);
        assertEquals(original.intValue(), copy.intValue());
        assertEquals(original.hashCode(), copy.hashCode());
    }

    /// Test copyInstance throws NullPointerException for null input.
    @SuppressWarnings("DataFlowIssue") // explicitly testing passing a null parameter
    @Test
    @DisplayName("Test copyInstance method with null input")
    public void testCopyInstanceWithNull() {
        assertThrows(NullPointerException.class, () -> provider.copyInstance(null));
    }

    /// Test the uniqueSizeLimit method returns Integer.MAX_VALUE.
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
        Supplier<Integer> supplier = provider.equalInstanceSupplier();

        Integer first = supplier.get();
        Integer second = supplier.get();
        Integer third = supplier.get();

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
        Supplier<Integer> supplier = provider.uniqueInstanceSupplier();

        Integer first = supplier.get();   // seed 0
        Integer second = supplier.get();  // seed 1
        Integer third = supplier.get();   // seed 2

        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);

        assertNotEquals(first, second);
        assertNotEquals(second, third);
        assertNotEquals(first, third);

        assertEquals(0, first.intValue());
        assertEquals(1, second.intValue());
        assertEquals(2, third.intValue());
    }

    /// Test uniqueInstanceSupplier with custom seed.
    @Test
    @DisplayName("Test uniqueInstanceSupplier method with custom seed")
    public void testUniqueInstanceSupplierWithSeed() {
        Supplier<Integer> supplier = provider.uniqueInstanceSupplier(10);

        Integer first = supplier.get();   // seed 10
        Integer second = supplier.get();  // seed 11
        Integer third = supplier.get();   // seed 12

        assertEquals(10, first.intValue());
        assertEquals(11, second.intValue());
        assertEquals(12, third.intValue());
    }

    /// Test randomInstanceSupplier produces valid instances.
    @Test
    @DisplayName("Test randomInstanceSupplier method")
    public void testRandomInstanceSupplier() {
        Supplier<Integer> supplier = provider.randomInstanceSupplier();

        Integer first = supplier.get();
        Integer second = supplier.get();
        Integer third = supplier.get();

        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);

        // Random instances should be valid Integer values within the valid range
        Assertions.assertGreaterThanOrEqual(first, Integer.MIN_VALUE);
        Assertions.assertLessThanOrEqual(first, Integer.MAX_VALUE);

        Assertions.assertGreaterThanOrEqual(second, Integer.MIN_VALUE);
        Assertions.assertLessThanOrEqual(second, Integer.MAX_VALUE);

        Assertions.assertGreaterThanOrEqual(third, Integer.MIN_VALUE);
        Assertions.assertLessThanOrEqual(third, Integer.MAX_VALUE);
    }

    // Tests for utility methods

    /// Test createEqualObjects method produces equal instances.
    /// @param size the size of the list of objects to create
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 10})
    @DisplayName("Test createEqualObjects method")
    public void testCreateEqualObjects(int size) {
        List<Integer> objects = provider.createEqualObjects(size);

        assertNotNull(objects);
        assertEquals(size, objects.size());

        if (size > 0) {
            Integer first = objects.getFirst();
            for (Integer obj : objects) {
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
        List<Integer> instances = provider.createUniqueInstances(size);

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
            assertEquals(i, instances.get(i).intValue());
        }
    }

    /// Test createUniqueInstances method with custom seed.
    @Test
    @DisplayName("Test createUniqueInstances method with custom seed")
    public void testCreateUniqueInstancesWithSeed() {
        int size = 5;
        int seed = 10;
        List<Integer> instances = provider.createUniqueInstances(size, seed);

        assertNotNull(instances);
        assertEquals(size, instances.size());

        // Check that values follow the expected pattern starting from seed
        for (int i = 0; i < instances.size(); i++) {
            assertEquals(seed + i, instances.get(i).intValue());
        }
    }

    /// Test createUniqueInstances throws exception when size exceeds limit.
    @Test
    @DisplayName("Test createUniqueInstances method with size exceeding limit")
    public void testCreateUniqueInstancesExceedsLimit() {
        // Create a mock provider with a smaller unique size limit for testing
        IntegerProvider limitedProvider = new IntegerProvider() {
            @Override
            public long uniqueSizeLimit() {
                return 5;
            }
        };

        assertThrows(IllegalArgumentException.class, () -> limitedProvider.createUniqueInstances(10));
    }

    /// Test createRandoInstances method produces valid instances.
    /// @param size the size of the list of objects to create
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 10})
    @DisplayName("Test createRandoInstances method")
    public void testCreateRandoInstances(int size) {
        List<Integer> instances = provider.createRandomInstances(size);

        assertNotNull(instances);
        assertEquals(size, instances.size());

        // All instances should be valid Integer values
        for (Integer instance : instances) {
            assertNotNull(instance);
            Assertions.assertGreaterThanOrEqual(instance, Integer.MIN_VALUE);
            Assertions.assertLessThanOrEqual(instance, Integer.MAX_VALUE);
        }
    }

    // Tests for consistency between related methods

    /// Test that defaultInstance and createValue(0) produce equivalent results.
    @Test
    @DisplayName("Test consistency between defaultInstance and createValue(0)")
    public void testDefaultInstanceConsistency() {
        Integer defaultValue = provider.defaultInstance();
        Integer zeroValue = provider.createValue(0);

        assertEquals(defaultValue, zeroValue);
        assertEquals(defaultValue.intValue(), zeroValue.intValue());
    }

    /// Test that createInstance(0) and createValue(0) produce equivalent results.
    @Test
    @DisplayName("Test consistency between createInstance(0) and createValue(0)")
    public void testCreateInstanceConsistency() {
        Integer fromSeed = provider.createInstance(0);
        Integer fromValue = provider.createValue(0);

        assertEquals(fromSeed, fromValue);
        assertEquals(fromSeed.intValue(), fromValue.intValue());
    }

    /// Test that maxValue and createValue(maxIntegerValue()) produce equivalent results.
    @Test
    @DisplayName("Test consistency between maxValue and createValue(maxIntegerValue())")
    public void testMaxValueConsistency() {
        Integer maxFromMethod = provider.maxValue();
        Integer maxFromPrimitive = provider.createValue(provider.maxIntegerValue());

        assertEquals(maxFromMethod, maxFromPrimitive);
        assertEquals(maxFromMethod.intValue(), maxFromPrimitive.intValue());
    }

    /// Test that minValue and createValue(minIntegerValue()) produce equivalent results.
    @Test
    @DisplayName("Test consistency between minValue and createValue(minIntegerValue())")
    public void testMinValueConsistency() {
        Integer minFromMethod = provider.minValue();
        Integer minFromPrimitive = provider.createValue(provider.minIntegerValue());

        assertEquals(minFromMethod, minFromPrimitive);
        assertEquals(minFromMethod.intValue(), minFromPrimitive.intValue());
    }

    // Edge case tests

    /// Test that createValue throws IllegalArgumentException for values outside Integer range.
    @Test
    @DisplayName("Test createValue with out-of-range values throws IllegalArgumentException")
    public void testCreateValueOutOfRange() {
        // Test values greater than Integer.MAX_VALUE
        long tooLarge = Integer.MAX_VALUE + 1L;
        IllegalArgumentException largeLongException = assertThrows(IllegalArgumentException.class,
                () -> provider.createValue(tooLarge));
        assertTrue(largeLongException.getMessage().contains("value (" + tooLarge + ") is not a valid Integer value"));

        // Test much larger values
        long veryLarge = Long.MAX_VALUE;
        IllegalArgumentException veryLargeException = assertThrows(IllegalArgumentException.class,
                () -> provider.createValue(veryLarge));
        assertTrue(veryLargeException.getMessage().contains("value (" + veryLarge + ") is not a valid Integer value"));

        // Test values less than Integer.MIN_VALUE
        long tooSmall = Integer.MIN_VALUE - 1L;
        IllegalArgumentException smallLongException = assertThrows(IllegalArgumentException.class,
                () -> provider.createValue(tooSmall));
        assertTrue(smallLongException.getMessage().contains("value (" + tooSmall + ") is not a valid Integer value"));

        // Test much smaller values
        long verySmall = Long.MIN_VALUE;
        IllegalArgumentException verySmallException = assertThrows(IllegalArgumentException.class,
                () -> provider.createValue(verySmall));
        assertTrue(verySmallException.getMessage().contains("value (" + verySmall + ") is not a valid Integer value"));

        // Test specific boundary violations
        long justAboveMax = (long) Integer.MAX_VALUE + 1;
        IllegalArgumentException justAboveMaxException = assertThrows(IllegalArgumentException.class,
                () -> provider.createValue(justAboveMax));
        assertTrue(justAboveMaxException.getMessage()
                .contains("value (" + justAboveMax + ") is not a valid Integer value"));

        long justBelowMin = (long) Integer.MIN_VALUE - 1;
        IllegalArgumentException justBelowMinException = assertThrows(IllegalArgumentException.class,
                () -> provider.createValue(justBelowMin));
        assertTrue(justBelowMinException.getMessage()
                .contains("value (" + justBelowMin + ") is not a valid Integer value"));
    }

    /// Test createValue boundary values that should work correctly.
    @Test
    @DisplayName("Test createValue with valid boundary values")
    public void testCreateValueValidBoundaries() {
        // Test that values exactly at the boundaries work correctly
        Integer maxValue = provider.createValue(Integer.MAX_VALUE);
        assertNotNull(maxValue);
        assertEquals(Integer.MAX_VALUE, maxValue.intValue());

        Integer minValue = provider.createValue(Integer.MIN_VALUE);
        assertNotNull(minValue);
        assertEquals(Integer.MIN_VALUE, minValue.intValue());

        // Test values just inside the boundaries
        Integer nearMax = provider.createValue(Integer.MAX_VALUE - 1);
        assertNotNull(nearMax);
        assertEquals(Integer.MAX_VALUE - 1, nearMax.intValue());

        Integer nearMin = provider.createValue(Integer.MIN_VALUE + 1);
        assertNotNull(nearMin);
        assertEquals(Integer.MIN_VALUE + 1, nearMin.intValue());

        // Test that conversion from long works correctly when in range
        long validLong1 = 1000000L;
        Integer fromLong1 = provider.createValue(validLong1);
        assertEquals(1000000, fromLong1.intValue());

        long validLong2 = -1000000L;
        Integer fromLong2 = provider.createValue(validLong2);
        assertEquals(-1000000, fromLong2.intValue());
    }

    /// Test behavior with extreme values.
    @Test
    @DisplayName("Test edge cases with extreme integer values")
    public void testExtremeIntegerValues() {
        // Test maximum value
        Integer maxInt = provider.createValue(Integer.MAX_VALUE);
        assertNotNull(maxInt);
        assertEquals(Integer.MAX_VALUE, maxInt.intValue());
        assertEquals(2147483647, maxInt.intValue());

        // Test minimum value
        Integer minInt = provider.createValue(Integer.MIN_VALUE);
        assertNotNull(minInt);
        assertEquals(Integer.MIN_VALUE, minInt.intValue());
        assertEquals(-2147483648, minInt.intValue());

        // Test zero
        Integer zero = provider.createValue(0);
        assertNotNull(zero);
        assertEquals(0, zero.intValue());

        // Test boundary values near zero
        Integer positiveOne = provider.createValue(1);
        Integer negativeOne = provider.createValue(-1);
        assertEquals(1, positiveOne.intValue());
        assertEquals(-1, negativeOne.intValue());
    }

    /// Test integer overflow scenarios at boundaries.
    @Test
    @DisplayName("Test integer boundary behavior")
    public void testIntegerBoundaries() {
        // Test that values are preserved correctly at boundaries
        Integer maxValue = provider.createValue(Integer.MAX_VALUE);
        Integer minValue = provider.createValue(Integer.MIN_VALUE);

        // Verify the values are exactly what we expect
        assertEquals(Integer.MAX_VALUE, maxValue.intValue());
        assertEquals(Integer.MIN_VALUE, minValue.intValue());

        // Test that copyInstance preserves boundary values
        Integer copiedMax = provider.copyInstance(maxValue);
        Integer copiedMin = provider.copyInstance(minValue);

        assertEquals(maxValue, copiedMax);
        assertEquals(minValue, copiedMin);
        assertEquals(maxValue.intValue(), copiedMax.intValue());
        assertEquals(minValue.intValue(), copiedMin.intValue());
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
        ObjectProvider<Integer> objectProvider = provider;
        NumberProvider<Integer> numberProvider = provider;
        IntegerNumberProvider<Integer> integerNumberProvider = provider;

        assertNotNull(objectProvider.defaultInstance());
        assertNotNull(numberProvider.maxValue());
        assertNotNull(integerNumberProvider.createValue(1));
    }

    // Tests for number conversion behavior

    /// Test that Integer instances work correctly with Number interface methods.
    @Test
    @DisplayName("Test Number interface conversion methods")
    public void testNumberConversions() {
        Integer instance = provider.createValue(42);

        // Test all Number conversion methods
        assertEquals(42, instance.intValue());
        assertEquals(42L, instance.longValue());
        assertEquals(42.0f, instance.floatValue(), 0.0f);
        assertEquals(42.0, instance.doubleValue(), 0.0);
        assertEquals((byte) 42, instance.byteValue());
        assertEquals((short) 42, instance.shortValue());
    }

    /// Test conversion behavior with boundary values.
    @Test
    @DisplayName("Test Number conversions with boundary values")
    public void testBoundaryNumberConversions() {
        Integer maxInt = provider.createValue(Integer.MAX_VALUE);
        Integer minInt = provider.createValue(Integer.MIN_VALUE);

        // Test conversions preserve the values correctly
        assertEquals(Integer.MAX_VALUE, maxInt.intValue());
        assertEquals(Integer.MAX_VALUE, maxInt.longValue());
        assertEquals(Integer.MIN_VALUE, minInt.intValue());
        assertEquals(Integer.MIN_VALUE, minInt.longValue());

        // Test that double conversion is exact for integer values
        assertEquals(Integer.MAX_VALUE, maxInt.doubleValue(), 0.0);
        assertEquals(Integer.MIN_VALUE, minInt.doubleValue(), 0.0);
    }

    /// Test hash code consistency for Integer values.
    @Test
    @DisplayName("Test hash code consistency")
    public void testHashCodeConsistency() {
        // Test that equal instances have equal hash codes
        Integer value1 = provider.createValue(100);
        Integer value2 = provider.createValue(100);
        Integer copied = provider.copyInstance(value1);

        assertEquals(value1, value2);
        assertEquals(value1, copied);
        assertEquals(value1.hashCode(), value2.hashCode());
        assertEquals(value1.hashCode(), copied.hashCode());

        // Test that different values have different hash codes (usually)
        Integer different = provider.createValue(200);
        assertNotEquals(value1, different);
        // Note: hash codes can collide, so we don't assert inequality
    }
}