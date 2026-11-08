package org.soliscode.test.breakable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for [Break], and that every [Break] constant of the breakable classes is distinct.
///
/// [Break] is a record, so two constants with the same description are equal, and an object given one of them has
/// the other too.
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

    @Test
    @DisplayName("every break constant is distinct from every other")
    void breakConstants_inAllBreakableClasses_areDistinct() throws Exception {
        Map<String, Break> constants = constants();
        assertTrue(constants.size() > 400, "expected the breakable classes' constants, found " + constants.size());

        Map<Break, List<String>> byBreak = new HashMap<>();
        constants.forEach((name, b) -> byBreak.computeIfAbsent(b, k -> new ArrayList<>()).add(name));
        List<List<String>> equalGroups = byBreak.values().stream().filter(names -> names.size() > 1).toList();
        assertEquals(List.of(), equalGroups, "break constants that are equal to each other");
    }

    @Test
    @DisplayName("every break constant has a description")
    void breakConstants_inAllBreakableClasses_haveDescriptions() throws Exception {
        constants().forEach((name, b) -> assertFalse(b.description().isBlank(), name + " has no description"));
    }

    @Test
    @DisplayName("a queue given the collection's remove break does not have the queue's, and the reverse")
    void queueRemoveBreaks_areSeparate() {
        BreakableQueue<Integer> collectionBreak = new BreakableQueue.Builder<Integer>()
                .addBreak(BreakableCollection.REMOVE_DOES_NOT_REMOVE_ELEMENT)
                .build();
        BreakableQueue<Integer> queueBreak = new BreakableQueue.Builder<Integer>()
                .addBreak(BreakableQueue.REMOVE_DOES_NOT_REMOVE_ELEMENT)
                .build();
        assertFalse(collectionBreak.hasBreak(BreakableQueue.REMOVE_DOES_NOT_REMOVE_ELEMENT));
        assertFalse(queueBreak.hasBreak(BreakableCollection.REMOVE_DOES_NOT_REMOVE_ELEMENT));
    }

    @Test
    @DisplayName("a deque given a SequencedCollection break does not have the deque's break of the same name")
    void dequeAndSequencedCollectionBreaks_areSeparate() {
        BreakableDeque<Integer> deque = new BreakableDeque.Builder<Integer>()
                .addBreak(BreakableSequencedCollection.GET_FIRST_RETURNS_NULL)
                .build();
        assertFalse(deque.hasBreak(BreakableDeque.GET_FIRST_RETURNS_NULL));
    }

    /// Returns every public static [Break] constant declared by a class of the breakable package, by its qualified
    /// field name. The classes are found in the directory of the package's compiled main classes, so a new breakable class
    /// is checked without being listed here.
    private static Map<String, Break> constants() throws ReflectiveOperationException, URISyntaxException {
        String pkg = Break.class.getPackageName();
        // The main classes' location, not the package's first match on the class path, which is the test classes
        URL url = Break.class.getProtectionDomain().getCodeSource().getLocation();
        assertNotNull(url, "cannot find the classes of " + pkg);
        File dir = new File(new File(url.toURI()), pkg.replace('.', File.separatorChar));
        File[] files = Objects.requireNonNull(dir.listFiles(), "cannot list " + dir);

        TreeSet<String> classNames = new TreeSet<>();
        for (File f : files) {
            String name = f.getName();
            if (name.endsWith(".class") && !name.equals("package-info.class")) {
                classNames.add(pkg + "." + name.substring(0, name.length() - ".class".length()));
            }
        }

        Map<String, Break> constants = new TreeMap<>();
        for (String className : classNames) {
            Class<?> c = Class.forName(className);
            for (Field f : c.getDeclaredFields()) {
                int m = f.getModifiers();
                if (f.getType() == Break.class && Modifier.isStatic(m) && Modifier.isPublic(m)) {
                    constants.put(c.getName().substring(pkg.length() + 1) + "." + f.getName(), (Break) f.get(null));
                }
            }
        }
        return constants;
    }
}
