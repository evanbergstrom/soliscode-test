package org.soliscode.test.provider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.soliscode.test.AbstractTest;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for the `DoubleProvider` class. These tests verify that the provider correctly implements
/// all inherited interface contracts and provides proper Double instances for testing purposes.
///
/// @author evanbergstrom
/// @since 1.0
/// @see DoubleProvider
@DisplayName("Tests for DoubleProvider")
public class DoubleProviderTest extends AbstractTest {

    private final DoubleProvider provider = Providers.doubleProvider();

    // Tests for DoubleNumberProvider interface methods

    /// Test that the createValue method correctly creates Double instances from primitive double values.
    /// @param value The primitive value to use for the provided objects.
    @ParameterizedTest
    @ValueSource(doubles = {0.0, 1.0, -1.0, 42.5, -42.5, Double.MAX_VALUE, Double.MIN_VALUE,
                           Double.MIN_NORMAL, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
    @DisplayName("Test createValue method with various double values")
    public void testCreateValue(double value) {
        Double result = provider.createValue(value);

        assertNotNull(result);
        assertEquals(value, result, 0.0);
        assertEquals(Double.valueOf(value), result);
    }

    /// Test createValue with NaN - requires special handling since NaN != NaN.
    @Test
    @DisplayName("Test createValue method with NaN")
    public void testCreateValueWithNaN() {
        Double result = provider.createValue(Double.NaN);

        assertNotNull(result);
        assertTrue(result.isNaN());
        assertTrue(Double.isNaN(result));
    }

    /// Test the maxPrimitiveValue method returns Double.MAX_VALUE.
    @Test
    @DisplayName("Test maxPrimitiveValue method")
    public void testMaxPrimitiveValue() {
        double maxValue = provider.maxPrimitiveValue();
        assertEquals(Double.MAX_VALUE, maxValue, 0.0);
    }

    /// Test the minPrimitiveValue method returns Double.MIN_VALUE.
    @Test
    @DisplayName("Test minPrimitiveValue method")
    public void testMinPrimitiveValue() {
        double minValue = provider.minPrimitiveValue();
        assertEquals(Double.MIN_VALUE, minValue, 0.0);
    }

    // Tests for NumberProvider interface methods

    /// Test the maxValue method returns a Double wrapping Double.MAX_VALUE.
    @Test
    @DisplayName("Test maxValue method")
    public void testMaxValue() {
        Double maxValue = provider.maxValue();

        assertNotNull(maxValue);
        assertEquals(Double.MAX_VALUE, maxValue, 0.0);
        assertEquals(Double.valueOf(Double.MAX_VALUE), maxValue);
    }

    /// Test the minValue method returns a Double wrapping Double.MIN_VALUE.
    /// Note: There's a bug in DoubleNumberProvider.minValue() - it returns maxPrimitiveValue() instead of minPrimitiveValue().
    @Test
    @DisplayName("Test minValue method (contains known bug)")
    public void testMinValue() {
        Double minValue = provider.minValue();

        assertNotNull(minValue);
        // This test documents the current buggy behavior where minValue returns MAX_VALUE
        assertEquals(Double.MIN_VALUE, minValue, 0.0);
        assertEquals(Double.valueOf(Double.MIN_VALUE), minValue);
    }

    // Tests for ObjectProvider interface methods

    /// Test the defaultInstance method returns a Double with value 0.0.
    @Test
    @DisplayName("Test defaultInstance method")
    public void testDefaultInstance() {
        Double defaultValue = provider.defaultInstance();

        assertNotNull(defaultValue);
        assertEquals(0.0, defaultValue, 0.0);
        assertEquals(Double.valueOf(0.0), defaultValue);
    }

    /// Test the createInstance(int) method with various seed values.
    /// @param seed The primitive value to use as the seed value for the provided objects.
    @ParameterizedTest
    @ValueSource(ints = {0, 1, -1, 42, -42, Integer.MAX_VALUE, Integer.MIN_VALUE})
    @DisplayName("Test createInstance(int) method with various seeds")
    public void testCreateInstanceWithSeed(int seed) {
        Double result = provider.createInstance(seed);

        assertNotNull(result);
        assertEquals((double) seed, result, 0.0);
        assertEquals(Double.valueOf(seed), result);
    }

    /// Test the no-argument createInstance method defaults to seed 0.
    @Test
    @DisplayName("Test no-argument createInstance method")
    public void testCreateInstanceNoArgs() {
        Double result = provider.createInstance();

        assertNotNull(result);
        assertEquals(0.0, result, 0.0);
        assertEquals(Double.valueOf(0.0), result);
    }

    /// Test the copyInstance method creates equal copies.
    /// @param value The primitive value to use for the provided objects.
    @ParameterizedTest
    @ValueSource(doubles = {0.0, 1.0, -1.0, 42.5, -42.5, Double.MAX_VALUE, Double.MIN_VALUE})
    @DisplayName("Test copyInstance method with various values")
    public void testCopyInstance(double value) {
        Double original = value;
        Double copy = provider.copyInstance(original);

        assertNotNull(copy);
        assertEquals(original, copy);
        assertEquals(original, copy, 0.0);
        assertEquals(original.hashCode(), copy.hashCode());
    }

    /// Test copyInstance with NaN requires special handling.
    @Test
    @DisplayName("Test copyInstance method with NaN")
    public void testCopyInstanceWithNaN() {
        Double original = Double.NaN;
        Double copy = provider.copyInstance(original);

        assertNotNull(copy);
        assertTrue(copy.isNaN());
        assertEquals(original.hashCode(), copy.hashCode());
        // Note: NaN.equals(NaN) is false, but Double.valueOf(NaN).equals(Double.valueOf(NaN)) is true
        assertEquals(original, copy);
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
        Supplier<Double> supplier = provider.equalInstanceSupplier();

        Double first = supplier.get();
        Double second = supplier.get();
        Double third = supplier.get();

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
        Supplier<Double> supplier = provider.uniqueInstanceSupplier();

        Double first = supplier.get();   // seed 0
        Double second = supplier.get();  // seed 1
        Double third = supplier.get();   // seed 2

        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);

        assertNotEquals(first, second);
        assertNotEquals(second, third);
        assertNotEquals(first, third);

        assertEquals(0.0, first, 0.0);
        assertEquals(1.0, second, 0.0);
        assertEquals(2.0, third, 0.0);
    }

    /// Test uniqueInstanceSupplier with custom seed.
    @Test
    @DisplayName("Test uniqueInstanceSupplier method with custom seed")
    public void testUniqueInstanceSupplierWithSeed() {
        Supplier<Double> supplier = provider.uniqueInstanceSupplier(10);

        Double first = supplier.get();   // seed 10
        Double second = supplier.get();  // seed 11
        Double third = supplier.get();   // seed 12

        assertEquals(10.0, first, 0.0);
        assertEquals(11.0, second, 0.0);
        assertEquals(12.0, third, 0.0);
    }

    /// Test randomInstanceSupplier produces valid instances.
    @Test
    @DisplayName("Test randomInstanceSupplier method")
    public void testRandomInstanceSupplier() {
        Supplier<Double> supplier = provider.randomInstanceSupplier();

        Double first = supplier.get();
        Double second = supplier.get();
        Double third = supplier.get();

        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);

        // Random instances should be valid Double values
        assertFalse(first.isNaN());
        assertFalse(second.isNaN());
        assertFalse(third.isNaN());

        assertTrue(Double.isFinite(first));
        assertTrue(Double.isFinite(second));
        assertTrue(Double.isFinite(third));
    }

    // Tests for utility methods

    /// Test createEqualObjects method produces equal instances.
    /// @param size the size of the list of objects to create
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 10})
    @DisplayName("Test createEqualObjects method")
    public void testCreateEqualObjects(int size) {
        List<Double> objects = provider.createEqualObjects(size);

        assertNotNull(objects);
        assertEquals(size, objects.size());

        if (size > 0) {
            Double first = objects.getFirst();
            for (Double obj : objects) {
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
        List<Double> instances = provider.createUniqueInstances(size);

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
            assertEquals((double) i, instances.get(i), 0.0);
        }
    }

    /// Test createUniqueInstances method with custom seed.
    @Test
    @DisplayName("Test createUniqueInstances method with custom seed")
    public void testCreateUniqueInstancesWithSeed() {
        int size = 5;
        int seed = 10;
        List<Double> instances = provider.createUniqueInstances(size, seed);

        assertNotNull(instances);
        assertEquals(size, instances.size());

        // Check that values follow the expected pattern starting from seed
        for (int i = 0; i < instances.size(); i++) {
            assertEquals((double) (seed + i), instances.get(i), 0.0);
        }
    }

    /// Test createUniqueInstances throws exception when size exceeds limit.
    @Test
    @DisplayName("Test createUniqueInstances method with size exceeding limit")
    public void testCreateUniqueInstancesExceedsLimit() {
        // Create a mock provider with a smaller unique size limit for testing
        DoubleProvider limitedProvider = new DoubleProvider() {
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
        List<Double> instances = provider.createRandomInstances(size);

        assertNotNull(instances);
        assertEquals(size, instances.size());

        // All instances should be valid Double values
        for (Double instance : instances) {
            assertNotNull(instance);
            assertFalse(instance.isNaN());
            assertTrue(Double.isFinite(instance));
        }
    }

    // Tests for consistency between related methods

    /// Test that defaultInstance and createValue(0.0) produce equivalent results.
    @Test
    @DisplayName("Test consistency between defaultInstance and createValue(0.0)")
    public void testDefaultInstanceConsistency() {
        Double defaultValue = provider.defaultInstance();
        Double zeroValue = provider.createValue(0.0);

        assertEquals(defaultValue, zeroValue);
        assertEquals(defaultValue, zeroValue, 0.0);
    }

    /// Test that createInstance(0) and createValue(0.0) produce equivalent results.
    @Test
    @DisplayName("Test consistency between createInstance(0) and createValue(0.0)")
    public void testCreateInstanceConsistency() {
        Double fromSeed = provider.createInstance(0);
        Double fromValue = provider.createValue(0.0);

        assertEquals(fromSeed, fromValue);
        assertEquals(fromSeed, fromValue, 0.0);
    }

    /// Test that maxValue and createValue(maxPrimitiveValue()) produce equivalent results.
    @Test
    @DisplayName("Test consistency between maxValue and createValue(maxPrimitiveValue())")
    public void testMaxValueConsistency() {
        Double maxFromMethod = provider.maxValue();
        Double maxFromPrimitive = provider.createValue(provider.maxPrimitiveValue());

        assertEquals(maxFromMethod, maxFromPrimitive);
        assertEquals(maxFromMethod, maxFromPrimitive, 0.0);
    }

    // Edge case tests

    /// Test behavior with extreme values and special cases.
    @SuppressWarnings("WrapperTypeMayBePrimitive")
    @Test
    @DisplayName("Test edge cases with special double values")
    public void testSpecialDoubleValues() {
        // Test positive and negative infinity
        Double posInf = provider.createValue(Double.POSITIVE_INFINITY);
        assertTrue(posInf.isInfinite());
        assertTrue(posInf > 0);

        Double negInf = provider.createValue(Double.NEGATIVE_INFINITY);
        assertTrue(negInf.isInfinite());
        assertTrue(negInf < 0);

        // Test positive and negative zero
        Double posZero = provider.createValue(0.0);
        Double negZero = provider.createValue(-0.0);

        // Test that the primitive values preserve their sign correctly
        assertEquals(0.0, posZero, 0.0);
        assertEquals(Double.doubleToRawLongBits(0.0), Double.doubleToRawLongBits(posZero));
        assertEquals(Double.doubleToRawLongBits(-0.0), Double.doubleToRawLongBits(negZero));

        // Verify that 0.0 and -0.0 compare equal but have different bit representations
        assertEquals(0.0, posZero, 0.0);
        assertEquals(0.0, negZero, 0.0);
        assertNotEquals(Double.doubleToRawLongBits(0.0), Double.doubleToRawLongBits(-0.0));

        // Test smallest positive normal value
        Double minNormal = provider.createValue(Double.MIN_NORMAL);
        assertNotNull(minNormal);
        assertEquals(Double.MIN_NORMAL, minNormal, 0.0);
    }

    /// Test that the provider follows all interface contracts properly.
    @SuppressWarnings("UnnecessaryLocalVariable")
    @Test
    @DisplayName("Test provider interface contracts")
    public void testProviderContracts() {
        // Test that the provider implements all expected interfaces
        assertInstanceOf(DoubleNumberProvider.class, provider);
        assertInstanceOf(NumberProvider.class, provider);
        assertInstanceOf(ObjectProvider.class, provider);

        // Test that generic type parameter is correctly specified
        ObjectProvider<Double> objectProvider = provider;
        NumberProvider<Double> numberProvider = provider;
        DoubleNumberProvider<Double> doubleNumberProvider = provider;

        assertNotNull(objectProvider.defaultInstance());
        assertNotNull(numberProvider.maxValue());
        assertNotNull(doubleNumberProvider.createValue(1.0));
    }
}