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

/// Tests for the `ShortProvider` class. These tests verify that the provider correctly implements
/// all inherited interface contracts and provides proper Short instances for testing purposes.
///
/// @author evanbergstrom
/// @since 1.0
/// @see ShortProvider
@DisplayName("Tests for ShortProvider")
public class ShortProviderTest extends AbstractTest {

    private final ShortProvider provider = Providers.shortProvider();

    // Tests for IntegerNumberProvider interface methods

    /// Test that the createValue method correctly creates Short instances from primitive short values.
    /// @param value The primitive value to use for the provided objects.
    @ParameterizedTest
    @ValueSource(shorts = {0, 1, -1, 42, -42, 100, -100, Short.MAX_VALUE, Short.MIN_VALUE})
    @DisplayName("Test createValue method with various integer values")
    public void testCreateValue(short value) {
        Short result = provider.createValue(value);

        assertNotNull(result);
        assertEquals(value, result.shortValue());
        assertEquals(Short.valueOf(value), result);
    }

    /// Test the maxIntegerValue method returns Short.MAX_VALUE.
    @Test
    @DisplayName("Test maxIntegerValue method")
    public void testMaxIntegerValue() {
        long maxValue = provider.maxIntegerValue();
        assertEquals(Short.MAX_VALUE, maxValue);
    }

    /// Test the minIntegerValue method returns Short.MIN_VALUE.
    @Test
    @DisplayName("Test minIntegerValue method")
    public void testMinIntegerValue() {
        long minValue = provider.minIntegerValue();
        assertEquals(Short.MIN_VALUE, minValue);
    }

    // Tests for NumberProvider interface methods

    /// Test the maxValue method returns an Short wrapping Short.MAX_VALUE.
    @Test
    @DisplayName("Test maxValue method")
    public void testMaxValue() {
        Short maxValue = provider.maxValue();

        assertNotNull(maxValue);
        assertEquals(Short.MAX_VALUE, maxValue.intValue());
        assertEquals(Short.valueOf(Short.MAX_VALUE), maxValue);
    }

    /// Test the minValue method returns an Short wrapping Short.MIN_VALUE.
    @Test
    @DisplayName("Test minValue method")
    public void testMinValue() {
        Short minValue = provider.minValue();

        assertNotNull(minValue);
        assertEquals(Short.MIN_VALUE, minValue.intValue());
        assertEquals(Short.valueOf(Short.MIN_VALUE), minValue);
    }

    // Tests for ObjectProvider interface methods

    /// Test the defaultInstance method returns an Short with value 0.
    @Test
    @DisplayName("Test defaultInstance method")
    public void testDefaultInstance() {
        Short defaultValue = provider.defaultInstance();

        assertNotNull(defaultValue);
        assertEquals(0, defaultValue.shortValue());
        assertEquals(Short.valueOf((short)0), defaultValue);
    }

    /// Test the createInstance(long) method with various seed values.
    /// @param seed The primitive value to use as the seed value for the provided objects.
    @ParameterizedTest
    @ValueSource(shorts = {0, 1, -1, 42, -42, 1000, -1000, Short.MAX_VALUE, Short.MIN_VALUE})
    @DisplayName("Test createInstance(long) method with various seeds")
    public void testCreateInstanceWithSeed(short seed) {
        Short result = provider.createInstance(seed);

        assertNotNull(result);
        assertEquals(seed, result.shortValue());
        assertEquals(Short.valueOf(seed), result);
    }

    /// Test the no-argument createInstance method defaults to seed 0.
    @Test
    @DisplayName("Test no-argument createInstance method")
    public void testCreateInstanceNoArgs() {
        Short result = provider.createInstance();

        assertNotNull(result);
        assertEquals(0, result.shortValue());
        assertEquals(Short.valueOf((short)0), result);
    }

    /// Test the copyInstance method creates equal copies.
    /// @param value The primitive value to use for the provided objects.
    @ParameterizedTest
    @ValueSource(shorts = {0, 1, -1, 42, -42, 1000, -1000, Short.MAX_VALUE, Short.MIN_VALUE})
    @DisplayName("Test copyInstance method with various values")
    public void testCopyInstance(short value) {
        Short original = value;
        Short copy = provider.copyInstance(original);

        assertNotNull(copy);
        assertEquals(original, copy);
        assertEquals(original.shortValue(), copy.shortValue());
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
        Supplier<Short> supplier = provider.equalInstanceSupplier();

        Short first = supplier.get();
        Short second = supplier.get();
        Short third = supplier.get();

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
        Supplier<Short> supplier = provider.uniqueInstanceSupplier();

        Short first = supplier.get();   // seed 0
        Short second = supplier.get();  // seed 1
        Short third = supplier.get();   // seed 2

        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);

        assertNotEquals(first, second);
        assertNotEquals(second, third);
        assertNotEquals(first, third);

        assertEquals(0, first.shortValue());
        assertEquals(1, second.intValue());
        assertEquals(2, third.intValue());
    }

    /// Test uniqueInstanceSupplier with custom seed.
    @Test
    @DisplayName("Test uniqueInstanceSupplier method with custom seed")
    public void testUniqueInstanceSupplierWithSeed() {
        Supplier<Short> supplier = provider.uniqueInstanceSupplier(10);

        Short first = supplier.get();   // seed 10
        Short second = supplier.get();  // seed 11
        Short third = supplier.get();   // seed 12

        assertEquals(10, first.shortValue());
        assertEquals(11, second.shortValue());
        assertEquals(12, third.shortValue());
    }

    /// Test randomInstanceSupplier produces valid instances.
    @Test
    @DisplayName("Test randomInstanceSupplier method")
    public void testRandomInstanceSupplier() {
        Supplier<Short> supplier = provider.randomInstanceSupplier();

        Short first = supplier.get();
        Short second = supplier.get();
        Short third = supplier.get();

        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);

        // Random instances should be valid Integer values within the valid range
        Assertions.assertGreaterThanOrEqual(first, Short.MIN_VALUE);
        Assertions.assertLessThanOrEqual(first, Short.MAX_VALUE);

        Assertions.assertGreaterThanOrEqual(second, Short.MIN_VALUE);
        Assertions.assertLessThanOrEqual(second, Short.MAX_VALUE);

        Assertions.assertGreaterThanOrEqual(third, Short.MIN_VALUE);
        Assertions.assertLessThanOrEqual(third, Short.MAX_VALUE);
    }

    // Tests for utility methods

    /// Test createEqualObjects method produces equal instances.
    /// @param size the size of the list of objects to create
    @ParameterizedTest
    @ValueSource(shorts = {0, 1, 5, 10})
    @DisplayName("Test createEqualObjects method")
    public void testCreateEqualObjects(int size) {
        List<Short> objects = provider.createEqualObjects(size);

        assertNotNull(objects);
        assertEquals(size, objects.size());

        if (size > 0) {
            Short first = objects.getFirst();
            for (Short obj : objects) {
                assertEquals(first, obj);
                assertEquals(first.hashCode(), obj.hashCode());
            }
        }
    }

    /// Test createUniqueInstances method produces unique instances.
    /// @param size the size of the list of objects to create
    @ParameterizedTest
    @ValueSource(shorts = {0, 1, 5, 10})
    @DisplayName("Test createUniqueInstances method")
    public void testCreateUniqueInstances(int size) {
        List<Short> instances = provider.createUniqueInstances(size);

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
        List<Short> instances = provider.createUniqueInstances(size, seed);

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
    @ValueSource(shorts = {0, 1, 5, 10})
    @DisplayName("Test createRandoInstances method")
    public void testCreateRandoInstances(int size) {
        List<Short> instances = provider.createRandomInstances(size);

        assertNotNull(instances);
        assertEquals(size, instances.size());

        // All instances should be valid Integer values
        for (Short instance : instances) {
            assertNotNull(instance);
            Assertions.assertGreaterThanOrEqual(instance, Short.MIN_VALUE);
            Assertions.assertLessThanOrEqual(instance, Short.MAX_VALUE);
        }
    }

    // Tests for consistency between related methods

    /// Test that defaultInstance and createValue(0) produce equivalent results.
    @Test
    @DisplayName("Test consistency between defaultInstance and createValue(0)")
    public void testDefaultInstanceConsistency() {
        Short defaultValue = provider.defaultInstance();
        Short zeroValue = provider.createValue(0);

        assertEquals(defaultValue, zeroValue);
        assertEquals(defaultValue.shortValue(), zeroValue.shortValue());
    }

    /// Test that createInstance(0) and createValue(0) produce equivalent results.
    @Test
    @DisplayName("Test consistency between createInstance(0) and createValue(0)")
    public void testCreateInstanceConsistency() {
        Short fromSeed = provider.createInstance(0);
        Short fromValue = provider.createValue(0);

        assertEquals(fromSeed, fromValue);
        assertEquals(fromSeed.shortValue(), fromValue.shortValue());
    }

    /// Test that maxValue and createValue(maxIntegerValue()) produce equivalent results.
    @Test
    @DisplayName("Test consistency between maxValue and createValue(maxIntegerValue())")
    public void testMaxValueConsistency() {
        Short maxFromMethod = provider.maxValue();
        Short maxFromPrimitive = provider.createValue(provider.maxIntegerValue());

        assertEquals(maxFromMethod, maxFromPrimitive);
        assertEquals(maxFromMethod.shortValue(), maxFromPrimitive.shortValue());
    }

    /// Test that minValue and createValue(minIntegerValue()) produce equivalent results.
    @Test
    @DisplayName("Test consistency between minValue and createValue(minIntegerValue())")
    public void testMinValueConsistency() {
        Short minFromMethod = provider.minValue();
        Short minFromPrimitive = provider.createValue(provider.minIntegerValue());

        assertEquals(minFromMethod, minFromPrimitive);
        assertEquals(minFromMethod.shortValue(), minFromPrimitive.shortValue());
    }

    // Edge case tests

    /// Test that createValue throws IllegalArgumentException for values outside Short range.
    @Test
    @DisplayName("Test createValue with out-of-range values throws IllegalArgumentException")
    public void testCreateValueOutOfRange() {
        // Test values greater than Short.MAX_VALUE
        long tooLarge = Short.MAX_VALUE + 1L;
        IllegalArgumentException largeLongException = assertThrows(IllegalArgumentException.class,
                () -> provider.createValue(tooLarge));
        assertTrue(largeLongException.getMessage().contains("value (" + tooLarge + ") is not a valid Short value"));

        // Test much larger values
        long veryLarge = Integer.MAX_VALUE;
        IllegalArgumentException veryLargeException = assertThrows(IllegalArgumentException.class,
                () -> provider.createValue(veryLarge));
        assertTrue(veryLargeException.getMessage().contains("value (" + veryLarge + ") is not a valid Short value"));

        // Test values less than Short.MIN_VALUE
        long tooSmall = Short.MIN_VALUE - 1L;
        IllegalArgumentException smallLongException = assertThrows(IllegalArgumentException.class,
                () -> provider.createValue(tooSmall));
        assertTrue(smallLongException.getMessage().contains("value (" + tooSmall + ") is not a valid Short value"));

        // Test much smaller values
        long verySmall = Integer.MIN_VALUE;
        IllegalArgumentException verySmallException = assertThrows(IllegalArgumentException.class,
                () -> provider.createValue(verySmall));
        assertTrue(verySmallException.getMessage().contains("value (" + verySmall + ") is not a valid Short value"));

        // Test extreme boundary values
        long maxLong = Long.MAX_VALUE;
        IllegalArgumentException maxLongException = assertThrows(IllegalArgumentException.class,
                () -> provider.createValue(maxLong));
        assertTrue(maxLongException.getMessage().contains("value (" + maxLong + ") is not a valid Short value"));

        long minLong = Long.MIN_VALUE;
        IllegalArgumentException minLongException = assertThrows(IllegalArgumentException.class,
                () -> provider.createValue(minLong));
        assertTrue(minLongException.getMessage().contains("value (" + minLong + ") is not a valid Short value"));
    }

    /// Test createValue boundary values that should work correctly.
    @Test
    @DisplayName("Test createValue with valid boundary values")
    public void testCreateValueValidBoundaries() {
        // Test that values exactly at the boundaries work correctly
        Short maxValue = provider.createValue(Short.MAX_VALUE);
        assertNotNull(maxValue);
        assertEquals(Short.MAX_VALUE, maxValue.shortValue());

        Short minValue = provider.createValue(Short.MIN_VALUE);
        assertNotNull(minValue);
        assertEquals(Short.MIN_VALUE, minValue.shortValue());

        // Test values just inside the boundaries
        Short nearMax = provider.createValue(Short.MAX_VALUE - 1);
        assertNotNull(nearMax);
        assertEquals(Short.MAX_VALUE - 1, nearMax.shortValue());

        Short nearMin = provider.createValue(Short.MIN_VALUE + 1);
        assertNotNull(nearMin);
        assertEquals(Short.MIN_VALUE + 1, nearMin.shortValue());
    }

    /// Test behavior with extreme values.
    @Test
    @DisplayName("Test edge cases with extreme integer values")
    public void testExtremeIntegerValues() {
        // Test maximum value
        Short maxInt = provider.createValue(Short.MAX_VALUE);
        assertNotNull(maxInt);
        assertEquals(Short.MAX_VALUE, maxInt.shortValue());

        // Test minimum value
        Short minInt = provider.createValue(Short.MIN_VALUE);
        assertNotNull(minInt);
        assertEquals(Short.MIN_VALUE, minInt.shortValue());

        // Test zero
        Short zero = provider.createValue(0);
        assertNotNull(zero);
        assertEquals(0, zero.intValue());

        // Test boundary values near zero
        Short positiveOne = provider.createValue(1);
        Short negativeOne = provider.createValue(-1);
        assertEquals(1, positiveOne.shortValue());
        assertEquals(-1, negativeOne.shortValue());
    }

    /// Test integer overflow scenarios at boundaries.
    @Test
    @DisplayName("Test integer boundary behavior")
    public void testIntegerBoundaries() {
        // Test that values are preserved correctly at boundaries
        Short maxValue = provider.createValue(Short.MAX_VALUE);
        Short minValue = provider.createValue(Short.MIN_VALUE);

        // Verify the values are exactly what we expect
        assertEquals(Short.MAX_VALUE, maxValue.shortValue());
        assertEquals(Short.MIN_VALUE, minValue.shortValue());

        // Test that copyInstance preserves boundary values
        Short copiedMax = provider.copyInstance(maxValue);
        Short copiedMin = provider.copyInstance(minValue);

        assertEquals(maxValue, copiedMax);
        assertEquals(minValue, copiedMin);
        assertEquals(maxValue.shortValue(), copiedMax.shortValue());
        assertEquals(minValue.shortValue(), copiedMin.shortValue());
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
        ObjectProvider<Short> objectProvider = provider;
        NumberProvider<Short> numberProvider = provider;
        IntegerNumberProvider<Short> integerNumberProvider = provider;

        assertNotNull(objectProvider.defaultInstance());
        assertNotNull(numberProvider.maxValue());
        assertNotNull(integerNumberProvider.createValue(1));
    }

    // Tests for number conversion behavior

    /// Test that Integer instances work correctly with Number interface methods.
    @Test
    @DisplayName("Test Number interface conversion methods")
    public void testNumberConversions() {
        Short instance = provider.createValue(42);

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
        Short maxInt = provider.createValue(Short.MAX_VALUE);
        Short minInt = provider.createValue(Short.MIN_VALUE);

        // Test conversions preserve the values correctly
        assertEquals(Short.MAX_VALUE, maxInt.shortValue());
        assertEquals(Short.MAX_VALUE, maxInt.longValue());
        assertEquals(Short.MIN_VALUE, minInt.shortValue());
        assertEquals(Short.MIN_VALUE, minInt.longValue());

        // Test that double conversion is exact for integer values
        assertEquals(Short.MAX_VALUE, maxInt.doubleValue(), 0.0);
        assertEquals(Short.MIN_VALUE, minInt.doubleValue(), 0.0);
    }

    /// Test hash code consistency for Integer values.
    @Test
    @DisplayName("Test hash code consistency")
    public void testHashCodeConsistency() {
        // Test that equal instances have equal hash codes
        Short value1 = provider.createValue(100);
        Short value2 = provider.createValue(100);
        Short copied = provider.copyInstance(value1);

        assertEquals(value1, value2);
        assertEquals(value1, copied);
        assertEquals(value1.hashCode(), value2.hashCode());
        assertEquals(value1.hashCode(), copied.hashCode());

        // Test that different values have different hash codes (usually)
        Short different = provider.createValue(200);
        assertNotEquals(value1, different);
        // Note: hash codes can collide, so we don't assert inequality
    }
}