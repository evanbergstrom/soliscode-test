package org.soliscode.test.provider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.soliscode.test.AbstractTest;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/// Tests for the `FloatProvider` class. These tests verify that the provider correctly implements
/// all inherited interface contracts and provides proper Float instances for testing purposes.
///
/// @author evanbergstrom
/// @since 1.0
/// @see FloatProvider
@DisplayName("Tests for FloatProvider")
public class FloatProviderTest extends AbstractTest {

    private final FloatProvider provider = Providers.floatProvider();

    // Tests for FloatNumberProvider interface methods

    /// Test that the createValue method correctly creates Float instances from primitive Float values.
    /// @param value The primitive value to use for the provided objects.
    @ParameterizedTest
    @ValueSource(floats = {0.0f, 1.0f, -1.0f, 42.5f, -42.5f, Float.MAX_VALUE, Float.MIN_VALUE,
                           Float.MIN_NORMAL, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY})
    @DisplayName("Test createValue method with various Float values")
    public void testCreateValue(float value) {
        Float result = provider.createValue(value);

        assertNotNull(result);
        assertEquals(value, result, 0.0f);
        assertEquals(Float.valueOf(value), result);
    }

    /// Test createValue with NaN - requires special handling since NaN != NaN.
    @Test
    @DisplayName("Test createValue method with NaN")
    public void testCreateValueWithNaN() {
        Float result = provider.createValue(Float.NaN);

        assertNotNull(result);
        assertTrue(result.isNaN());
        assertTrue(Float.isNaN(result));
    }

    /// Test the maxPrimitiveValue method returns Float.MAX_VALUE.
    @Test
    @DisplayName("Test maxPrimitiveValue method")
    public void testMaxPrimitiveValue() {
        double maxValue = provider.maxPrimitiveValue();
        assertEquals(Float.MAX_VALUE, maxValue, 0.0f);
    }

    /// Test the minPrimitiveValue method returns Float.MIN_VALUE.
    @Test
    @DisplayName("Test minPrimitiveValue method")
    public void testMinPrimitiveValue() {
        double minValue = provider.minPrimitiveValue();
        assertEquals(Float.MIN_VALUE, minValue, 0.0f);
    }

    // Tests for NumberProvider interface methods

    /// Test the maxValue method returns a Float wrapping Float.MAX_VALUE.
    @Test
    @DisplayName("Test maxValue method")
    public void testMaxValue() {
        Float maxValue = provider.maxValue();

        assertNotNull(maxValue);
        assertEquals(Float.MAX_VALUE, maxValue, 0.0f);
        assertEquals(Float.valueOf(Float.MAX_VALUE), maxValue);
    }

    /// Test the minValue method returns a Float wrapping Float.MIN_VALUE.
    @Test
    @DisplayName("Test minValue method (contains known bug)")
    public void testMinValue() {
        Float minValue = provider.minValue();

        assertNotNull(minValue);
        // This test documents the current buggy behavior where minValue returns MAX_VALUE
        assertEquals(Float.MIN_VALUE, minValue, 0.0f);
        assertEquals(Float.valueOf(Float.MIN_VALUE), minValue);
    }

    // Tests for ObjectProvider interface methods

    /// Test the defaultInstance method returns a Float with value 0.0.
    @Test
    @DisplayName("Test defaultInstance method")
    public void testDefaultInstance() {
        Float defaultValue = provider.defaultInstance();

        assertNotNull(defaultValue);
        assertEquals(0.0f, defaultValue, 0.0f);
        assertEquals(Float.valueOf(0.0f), defaultValue);
    }

    /// Test the createInstance(int) method with various seed values.
    /// @param seed The primitive value to use as the seed value for the provided objects.
    @ParameterizedTest
    @ValueSource(ints = {0, 1, -1, 42, -42, Integer.MAX_VALUE, Integer.MIN_VALUE})
    @DisplayName("Test createInstance(int) method with various seeds")
    public void testCreateInstanceWithSeed(int seed) {
        Float result = provider.createInstance(seed);

        assertNotNull(result);
        assertEquals((float) seed, result, 0.0f);
        assertEquals(Float.valueOf(seed), result);
    }

    /// Test the no-argument createInstance method defaults to seed 0.
    @Test
    @DisplayName("Test no-argument createInstance method")
    public void testCreateInstanceNoArgs() {
        Float result = provider.createInstance();

        assertNotNull(result);
        assertEquals(0.0f, result, 0.0f);
        assertEquals(Float.valueOf(0.0f), result);
    }

    /// Test the copyInstance method creates equal copies.
    /// @param value The primitive value to use for the provided objects.
    @ParameterizedTest
    @ValueSource(floats = {0.0f, 1.0f, -1.0f, 42.5f, -42.5f, Float.MAX_VALUE, Float.MIN_VALUE})
    @DisplayName("Test copyInstance method with various values")
    public void testCopyInstance(float value) {
        Float original = value;
        Float copy = provider.copyInstance(original);

        assertNotNull(copy);
        assertEquals(original, copy);
        assertEquals(original, copy, 0.0f);
        assertEquals(original.hashCode(), copy.hashCode());
    }

    /// Test copyInstance with NaN requires special handling.
    @Test
    @DisplayName("Test copyInstance method with NaN")
    public void testCopyInstanceWithNaN() {
        Float original = Float.NaN;
        Float copy = provider.copyInstance(original);

        assertNotNull(copy);
        assertTrue(copy.isNaN());
        assertEquals(original.hashCode(), copy.hashCode());
        // Note: NaN.equals(NaN) is false, but Float.valueOf(NaN).equals(Float.valueOf(NaN)) is true
        assertEquals(original, copy);
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
        Supplier<Float> supplier = provider.equalInstanceSupplier();

        Float first = supplier.get();
        Float second = supplier.get();
        Float third = supplier.get();

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
        Supplier<Float> supplier = provider.uniqueInstanceSupplier();

        Float first = supplier.get();   // seed 0
        Float second = supplier.get();  // seed 1
        Float third = supplier.get();   // seed 2

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
        Supplier<Float> supplier = provider.uniqueInstanceSupplier(10);

        Float first = supplier.get();   // seed 10
        Float second = supplier.get();  // seed 11
        Float third = supplier.get();   // seed 12

        assertEquals(10.0, first, 0.0);
        assertEquals(11.0, second, 0.0);
        assertEquals(12.0, third, 0.0);
    }

    /// Test randomInstanceSupplier produces valid instances.
    @Test
    @DisplayName("Test randomInstanceSupplier method")
    public void testRandomInstanceSupplier() {
        Supplier<Float> supplier = provider.randomInstanceSupplier();

        Float first = supplier.get();
        Float second = supplier.get();
        Float third = supplier.get();

        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);

        // Random instances should be valid Float values
        assertFalse(first.isNaN());
        assertFalse(second.isNaN());
        assertFalse(third.isNaN());

        assertTrue(Float.isFinite(first));
        assertTrue(Float.isFinite(second));
        assertTrue(Float.isFinite(third));
    }

    // Tests for utility methods

    /// Test createEqualObjects method produces equal instances.
    /// @param size the size of the list of objects to create
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 10})
    @DisplayName("Test createEqualObjects method")
    public void testCreateEqualObjects(int size) {
        List<Float> objects = provider.createEqualObjects(size);

        assertNotNull(objects);
        assertEquals(size, objects.size());

        if (size > 0) {
            Float first = objects.getFirst();
            for (Float obj : objects) {
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
        List<Float> instances = provider.createUniqueInstances(size);

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
            assertEquals((float) i, instances.get(i), 0.0);
        }
    }

    /// Test createUniqueInstances method with custom seed.
    @Test
    @DisplayName("Test createUniqueInstances method with custom seed")
    public void testCreateUniqueInstancesWithSeed() {
        int size = 5;
        int seed = 10;
        List<Float> instances = provider.createUniqueInstances(size, seed);

        assertNotNull(instances);
        assertEquals(size, instances.size());

        // Check that values follow the expected pattern starting from seed
        for (int i = 0; i < instances.size(); i++) {
            assertEquals((float) (seed + i), instances.get(i), 0.0);
        }
    }

    /// Test createUniqueInstances throws exception when size exceeds limit.
    @Test
    @DisplayName("Test createUniqueInstances method with size exceeding limit")
    public void testCreateUniqueInstancesExceedsLimit() {
        // Create a mock provider with a smaller unique size limit for testing
        FloatProvider limitedProvider = new FloatProvider() {
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
        List<Float> instances = provider.createRandomInstances(size);

        assertNotNull(instances);
        assertEquals(size, instances.size());

        // All instances should be valid Float values
        for (Float instance : instances) {
            assertNotNull(instance);
            assertFalse(instance.isNaN());
            assertTrue(Float.isFinite(instance));
        }
    }

    // Tests for consistency between related methods

    /// Test that defaultInstance and createValue(0.0) produce equivalent results.
    @Test
    @DisplayName("Test consistency between defaultInstance and createValue(0.0)")
    public void testDefaultInstanceConsistency() {
        Float defaultValue = provider.defaultInstance();
        Float zeroValue = provider.createValue(0.0);

        assertEquals(defaultValue, zeroValue);
        assertEquals(defaultValue, zeroValue, 0.0);
    }

    /// Test that createInstance(0) and createValue(0.0) produce equivalent results.
    @Test
    @DisplayName("Test consistency between createInstance(0) and createValue(0.0)")
    public void testCreateInstanceConsistency() {
        Float fromSeed = provider.createInstance(0);
        Float fromValue = provider.createValue(0.0);

        assertEquals(fromSeed, fromValue);
        assertEquals(fromSeed, fromValue, 0.0);
    }

    /// Test that maxValue and createValue(maxPrimitiveValue()) produce equivalent results.
    @Test
    @DisplayName("Test consistency between maxValue and createValue(maxPrimitiveValue())")
    public void testMaxValueConsistency() {
        Float maxFromMethod = provider.maxValue();
        Float maxFromPrimitive = provider.createValue(provider.maxPrimitiveValue());

        assertEquals(maxFromMethod, maxFromPrimitive);
        assertEquals(maxFromMethod, maxFromPrimitive, 0.0f);
    }

    // Edge case tests

    /// Test behavior with extreme values and special cases.
    @SuppressWarnings("WrapperTypeMayBePrimitive")
    @Test
    @DisplayName("Test edge cases with special Float values")
    public void testSpecialFloatValues() {
        // Test positive and negative infinity
        Float posInf = provider.createValue(Float.POSITIVE_INFINITY);
        assertTrue(posInf.isInfinite());
        assertTrue(posInf > 0);

        Float negInf = provider.createValue(Float.NEGATIVE_INFINITY);
        assertTrue(negInf.isInfinite());
        assertTrue(negInf < 0);

        // Test positive and negative zero
        Float posZero = provider.createValue(0.0f);
        Float negZero = provider.createValue(-0.0f);

        // Test that the primitive values preserve their sign correctly
        assertEquals(0.0, posZero, 0.0);
        assertEquals(Float.floatToRawIntBits(0.0f), Float.floatToRawIntBits(posZero));
        assertEquals(Float.floatToRawIntBits(-0.0f), Float.floatToRawIntBits(negZero));

        // Verify that 0.0 and -0.0 compare equal but have different bit representations
        assertEquals(0.0, posZero, 0.0);
        assertEquals(0.0, negZero, 0.0);
        assertNotEquals(Float.floatToRawIntBits(0.0f), Float.floatToRawIntBits(-0.0f));

        // Test smallest positive normal value
        Float minNormal = provider.createValue(Float.MIN_NORMAL);
        assertNotNull(minNormal);
        assertEquals(Float.MIN_NORMAL, minNormal, 0.0);
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
        ObjectProvider<Float> objectProvider = provider;
        NumberProvider<Float> numberProvider = provider;
        DoubleNumberProvider<Float> FloatNumberProvider = provider;

        assertNotNull(objectProvider.defaultInstance());
        assertNotNull(numberProvider.maxValue());
        assertNotNull(FloatNumberProvider.createValue(1.0));
    }
}