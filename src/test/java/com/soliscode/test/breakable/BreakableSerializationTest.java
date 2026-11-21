/*
 * Copyright 2025 Evan Bergstrom
 * SPDX-License-Identifier: Apache-2.0
 */

package com.soliscode.test.breakable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.soliscode.test.InterfaceMethod;
import com.soliscode.test.MethodStatus;
import com.soliscode.test.methods.CollectionMethods;
import com.soliscode.test.methods.MapMethods;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests that a breakable's method statuses survive serialization (#59): the round trip of a map, an explicitly
/// supported status, a stream written by the released 0.1.4, and the refusal of a malformed stream. The round trip of
/// a collection's unsupported method is in [BreakableCollectionTest].
public class BreakableSerializationTest {

    /// The fixture: a `BreakableCollection<Integer>` of 1, 2 and 3, with `ADD_DOES_NOT_ADD_ELEMENT` and
    /// `setSafe(true)`, serialized by the released `com.soliscode:soliscode-test:0.1.4` jar. It was made once, by hand,
    /// by running `Breakables.buildCollection(1, 2, 3).addBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT)
    /// .setSafe(true).build()` through an `ObjectOutputStream` with only that jar and JSpecify on the class path.
    private static final String FIXTURE_0_1_4 = "breakable-collection-0.1.4.ser";

    /// Default constructor.
    public BreakableSerializationTest() {
        super();
    }

    @Test
    @DisplayName("the breakables whose class changed since 0.1.4 keep 0.1.4's serialVersionUID")
    void changedBreakablesKeepTheirSerialVersionUid() {
        // The values serialver gives for 0.1.4's jar: a change to the class would change the computed ones, and 0.1.4
        // streams would no longer read (#64, #67).
        assertEquals(1825535398663778434L, ObjectStreamClass.lookup(BreakableIterable.class).getSerialVersionUID(),
                "BreakableIterable");
        assertEquals(-8492110436425117573L, ObjectStreamClass.lookup(BreakableSortedSet.class).getSerialVersionUID(),
                "BreakableSortedSet");
        assertEquals(-322284699396048271L,
                ObjectStreamClass.lookup(BreakableNavigableSet.class).getSerialVersionUID(), "BreakableNavigableSet");
    }

    /// Verifies that a serialization round trip keeps a map's unsupported methods, and its safety.
    ///
    /// @throws IOException if an I/O error occurs
    /// @throws ClassNotFoundException if the class of a serialized object cannot be found
    @Test
    @DisplayName("serialization round-trip keeps an unsupported method unsupported and a safe map safe")
    public void serializeMapWhenMethodUnsupportedKeepsItUnsupported() throws IOException, ClassNotFoundException {
        BreakableMap<String, Integer> original = new BreakableMap.Builder<String, Integer>()
                .doesNotSupport(MapMethods.PUT)
                .setSafe(true)
                .build();

        BreakableMap<?, ?> deserialized = (BreakableMap<?, ?>) read(write(original, UnaryOperator.identity()));

        assertFalse(deserialized.supportsMethod(MapMethods.PUT), "Deserialized map should not support put");
        // null fits the wildcard types; put checks its support before its arguments.
        assertThrows(UnsupportedOperationException.class, () -> deserialized.put(null, null),
                "Deserialized map should refuse put");
        assertTrue(deserialized.supportsMethod(MapMethods.GET), "Deserialized map should support get");
        assertTrue(deserialized.isSafe(), "Deserialized map should stay safe");
    }

    /// Verifies that a serialization round trip keeps an explicitly supported status as well as an unsupported one,
    /// which only [com.soliscode.test.MethodSupport]'s `methodStatuses()` tells apart from no status.
    ///
    /// @throws IOException if an I/O error occurs
    /// @throws ClassNotFoundException if the class of a serialized object cannot be found
    @Test
    @DisplayName("serialization round-trip keeps explicitly supported and unsupported statuses")
    public void serializeWhenStatusesExplicitKeepsThem() throws IOException, ClassNotFoundException {
        Map<InterfaceMethod, MethodStatus> statuses = new HashMap<>();
        statuses.put(CollectionMethods.ADD, MethodStatus.UNSUPPORTED);
        statuses.put(CollectionMethods.REMOVE, MethodStatus.SUPPORTED);
        StatusCollection original = new StatusCollection(statuses);

        StatusCollection deserialized = (StatusCollection) read(write(original, UnaryOperator.identity()));

        assertEquals(statuses, deserialized.statuses(), "Deserialized collection should keep every status");
    }

    /// Verifies that a collection serialized by the released 0.1.4, whose stream has no method statuses, reads with
    /// its elements, breaks and safety, and with every method supported.
    ///
    /// @throws IOException if an I/O error occurs
    /// @throws ClassNotFoundException if the class of a serialized object cannot be found
    @Test
    @DisplayName("a collection serialized by 0.1.4 reads with its elements, break and safety")
    public void deserializeWhenWrittenByReleaseReadsIt() throws IOException, ClassNotFoundException {
        byte[] bytes;
        InputStream resource = BreakableSerializationTest.class.getResourceAsStream(FIXTURE_0_1_4);
        try (InputStream in = Objects.requireNonNull(resource, FIXTURE_0_1_4)) {
            bytes = in.readAllBytes();
        }

        BreakableCollection<?> deserialized = (BreakableCollection<?>) read(bytes);

        assertArrayEquals(new Object[] {1, 2, 3}, deserialized.toArray(), "Should keep the elements");
        assertTrue(deserialized.hasBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT), "Should keep the break");
        assertTrue(deserialized.isSafe(), "Should stay safe");
        assertTrue(deserialized.supportsAllMethods(), "Should support every method");
    }

    /// Verifies that a stream with the same method twice in one status array is refused.
    ///
    /// @throws IOException if an I/O error occurs
    @Test
    @DisplayName("a stream with a method twice in one status array is refused")
    public void deserializeWhenMethodRepeatedThrowsInvalidObjectException() throws IOException {
        assertRefused(replaceStatusArray(true, new InterfaceMethod[] {CollectionMethods.ADD, CollectionMethods.ADD}));
    }

    /// Verifies that a stream with the same method in both status arrays is refused.
    ///
    /// @throws IOException if an I/O error occurs
    @Test
    @DisplayName("a stream with a method in both status arrays is refused")
    public void deserializeWhenMethodInBothArraysThrowsInvalidObjectException() throws IOException {
        assertRefused(replaceStatusArray(false, new InterfaceMethod[] {CollectionMethods.ADD}));
    }

    /// Verifies that a stream with a null method in a status array is refused.
    ///
    /// @throws IOException if an I/O error occurs
    @Test
    @DisplayName("a stream with a null method is refused")
    public void deserializeWhenMethodNullThrowsInvalidObjectException() throws IOException {
        assertRefused(replaceStatusArray(true, new InterfaceMethod[] {null}));
    }

    /// Verifies that a stream whose status field is not an array of methods is refused.
    ///
    /// @throws IOException if an I/O error occurs
    @Test
    @DisplayName("a stream whose status field is not an array of methods is refused")
    public void deserializeWhenStatusFieldWrongTypeThrowsInvalidObjectException() throws IOException {
        assertRefused(replaceStatusArray(true, new String[] {"add"}));
    }

    /// Verifies that a stream whose breaks hold something other than a [Break] is refused.
    ///
    /// @throws IOException if an I/O error occurs
    @Test
    @DisplayName("a stream whose breaks hold a non-Break is refused")
    public void deserializeWhenBreaksHoldNonBreakThrowsInvalidObjectException() throws IOException {
        byte[] bytes = write(unsupportedAdd(), (obj) -> obj instanceof Set<?> set
                && set.contains(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT) ? new HashSet<>(Set.of("break")) : obj);
        assertRefused(bytes);
    }

    /// Verifies that a stream whose breaks are not a set is refused.
    ///
    /// @throws IOException if an I/O error occurs
    @Test
    @DisplayName("a stream whose breaks are not a set is refused")
    public void deserializeWhenBreaksNotSetThrowsInvalidObjectException() throws IOException {
        byte[] bytes = write(unsupportedAdd(), (obj) -> obj instanceof Set<?> set
                && set.contains(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT) ? "breaks" : obj);
        assertRefused(bytes);
    }

    /// Returns the stream of [#unsupportedAdd()] with one of its status arrays replaced: the unsupported methods'
    /// (which holds `ADD`) if `unsupported` is true, and the supported methods' (which is empty) if not.
    private static byte[] replaceStatusArray(final boolean unsupported, final Object replacement) throws IOException {
        return write(unsupportedAdd(), (obj) -> {
            if (obj instanceof InterfaceMethod[] methods
                    && Arrays.asList(methods).contains(CollectionMethods.ADD) == unsupported) {
                return replacement;
            }
            return obj;
        });
    }

    /// Returns a collection with `ADD` unsupported and the break `ADD_DOES_NOT_ADD_ELEMENT`.
    private static BreakableCollection<Integer> unsupportedAdd() {
        return Breakables.buildCollection(1, 2, 3)
                .addBreak(BreakableCollection.ADD_DOES_NOT_ADD_ELEMENT)
                .doesNotSupport(CollectionMethods.ADD)
                .build();
    }

    /// Asserts that reading the stream throws [InvalidObjectException].
    private static void assertRefused(final byte[] bytes) {
        assertThrows(InvalidObjectException.class, () -> read(bytes), "The malformed stream should be refused");
    }

    /// Serializes the object, passing each object written through `replacer` first.
    private static byte[] write(final Object object, final UnaryOperator<Object> replacer) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ReplacingOutputStream(bytes, replacer)) {
            out.writeObject(object);
        }
        return bytes.toByteArray();
    }

    /// Deserializes one object.
    private static Object read(final byte[] bytes) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return in.readObject();
        }
    }

    /// An object stream that replaces each object it writes by its replacer's result, to craft malformed streams.
    private static final class ReplacingOutputStream extends ObjectOutputStream {

        /// What each object written is replaced by.
        private final UnaryOperator<Object> replacer;

        /// Creates the stream.
        ReplacingOutputStream(final OutputStream out, final UnaryOperator<Object> replacerArg) throws IOException {
            super(out);
            this.replacer = replacerArg;
            enableReplaceObject(true);
        }

        @Override
        protected Object replaceObject(final Object obj) {
            return replacer.apply(obj);
        }
    }

    /// A collection that exposes its method statuses. It is a static class, since an inner one would capture the
    /// test, which is not serializable.
    private static final class StatusCollection extends BreakableCollection<Integer> {

        @Serial
        private static final long serialVersionUID = 1L;

        /// Creates a collection of 1 and 2 with the given statuses.
        StatusCollection(final Map<InterfaceMethod, MethodStatus> statuses) {
            super(new ArrayList<>(List.of(1, 2)), new HashSet<>(), statuses, DEFAULT_CHARACTERISTICS, DEFAULT_PERMITS,
                    false, Integer.class);
        }

        /// Returns the collection's method statuses.
        Map<InterfaceMethod, MethodStatus> statuses() {
            return methodStatuses();
        }
    }
}
