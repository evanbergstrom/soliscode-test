/*
 * Copyright 2026 Evan Bergstrom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.soliscode.test.performance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/// The points of a parameter matrix, each a case of a scenario.
///
/// A sweep is the cross product of its parameters' values, in the order they were given, the first parameter
/// changing slowest; [#points()] is what a `PerformanceScenario`'s `points()` returns.
///
/// ## Usage Examples
/// ```java
/// // threads 1, 4, 8, each at rates 1000 and 10000: six points
/// Sweep sweep = Sweep.of("threads", 1, 4, 8).and("rate", 1_000, 10_000);
/// List<Map<String, String>> points = sweep.points();
/// int threads = Sweep.intParameter(points.get(0), "threads");
/// ```
///
/// ## Thread Safety
/// Immutable.
///
/// @since 0.1.6
public final class Sweep {

    private final List<Parameter> parameters;
    private final List<Predicate<Map<String, String>>> filters;

    private Sweep(final List<Parameter> parametersArg, final List<Predicate<Map<String, String>>> filtersArg) {
        this.parameters = List.copyOf(parametersArg);
        this.filters = List.copyOf(filtersArg);
    }

    /// Returns a sweep of one parameter.
    ///
    /// @param name the parameter's name, not blank
    /// @param first its first value
    /// @param rest its other values, each given once
    /// @return the sweep
    /// @throws IllegalArgumentException if the name is blank or a value is given twice
    public static Sweep of(final String name, final String first, final String... rest) {
        return new Sweep(List.of(parameter(name, first, rest)), List.of());
    }

    /// Returns a sweep of one numeric parameter.
    ///
    /// @param name the parameter's name, not blank
    /// @param first its first value
    /// @param rest its other values, each given once
    /// @return the sweep
    /// @throws IllegalArgumentException if the name is blank or a value is given twice
    public static Sweep of(final String name, final int first, final int... rest) {
        return of(name, Integer.toString(first), strings(rest));
    }

    /// Returns this sweep crossed with one more parameter, which changes faster than those before it.
    ///
    /// @param name the parameter's name, not blank and not already in the sweep
    /// @param first its first value
    /// @param rest its other values, each given once
    /// @return the new sweep
    /// @throws IllegalArgumentException if the name is blank or already in the sweep, or a value is given twice
    public Sweep and(final String name, final String first, final String... rest) {
        Parameter added = parameter(name, first, rest);
        for (final Parameter parameter : parameters) {
            if (parameter.name().equals(added.name())) {
                throw new IllegalArgumentException("the parameter " + name + " is already in the sweep");
            }
        }
        List<Parameter> crossed = new ArrayList<>(parameters);
        crossed.add(added);
        return new Sweep(crossed, filters);
    }

    /// Returns this sweep crossed with one more numeric parameter, which changes faster than those before it.
    ///
    /// @param name the parameter's name, not blank and not already in the sweep
    /// @param first its first value
    /// @param rest its other values, each given once
    /// @return the new sweep
    /// @throws IllegalArgumentException if the name is blank or already in the sweep, or a value is given twice
    public Sweep and(final String name, final int first, final int... rest) {
        return and(name, Integer.toString(first), strings(rest));
    }

    /// Returns this sweep without the points the predicate refuses.
    ///
    /// @param keep whether a point stays
    /// @return the new sweep
    public Sweep where(final Predicate<Map<String, String>> keep) {
        Objects.requireNonNull(keep, "keep");
        List<Predicate<Map<String, String>>> kept = new ArrayList<>(filters);
        kept.add(keep);
        return new Sweep(parameters, kept);
    }

    /// Returns the sweep's points: each an unmodifiable map of every parameter to one of its values, in parameter
    /// order, the first parameter changing slowest.
    ///
    /// @return the points, at least one
    /// @throws IllegalStateException if the predicates of [#where(Predicate)] refuse every point
    public List<Map<String, String>> points() {
        List<Map<String, String>> points = new ArrayList<>();
        addPoints(0, new LinkedHashMap<>(), points);
        if (points.isEmpty()) {
            throw new IllegalStateException("the sweep's predicates refuse every point of " + parameters);
        }
        return List.copyOf(points);
    }

    /// Returns a point's value of a parameter.
    ///
    /// @param point the point
    /// @param name the parameter's name
    /// @return its value
    /// @throws IllegalArgumentException if the point has no such parameter
    public static String parameter(final Map<String, String> point, final String name) {
        Objects.requireNonNull(point, "point");
        Objects.requireNonNull(name, "name");
        String value = point.get(name);
        if (value == null) {
            throw new IllegalArgumentException("the point " + point + " has no parameter " + name);
        }
        return value;
    }

    /// Returns a point's value of a numeric parameter.
    ///
    /// @param point the point
    /// @param name the parameter's name
    /// @return its value
    /// @throws IllegalArgumentException if the point has no such parameter, or its value is not an `int`
    public static int intParameter(final Map<String, String> point, final String name) {
        String value = parameter(point, name);
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("the parameter " + name + " is " + value + ", not an int", e);
        }
    }

    private void addPoints(final int index, final Map<String, String> partial,
        final List<Map<String, String>> points) {
        if (index == parameters.size()) {
            Map<String, String> point = Collections.unmodifiableMap(new LinkedHashMap<>(partial));
            if (filters.stream().allMatch(filter -> filter.test(point))) {
                points.add(point);
            }
            return;
        }
        Parameter parameter = parameters.get(index);
        for (final String value : parameter.values()) {
            partial.put(parameter.name(), value);
            addPoints(index + 1, partial, points);
        }
        partial.remove(parameter.name());
    }

    private static Parameter parameter(final String name, final String first, final String... rest) {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("a sweep's parameter name is blank");
        }
        List<String> values = new ArrayList<>(rest.length + 1);
        values.add(Objects.requireNonNull(first, "first"));
        for (final String value : rest) {
            values.add(Objects.requireNonNull(value, "a value of " + name));
        }
        Set<String> seen = new HashSet<>();
        for (final String value : values) {
            if (!seen.add(value)) {
                throw new IllegalArgumentException("the parameter " + name + " has the value " + value + " twice");
            }
        }
        return new Parameter(name, values);
    }

    private static String[] strings(final int... values) {
        String[] strings = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            strings[i] = Integer.toString(values[i]);
        }
        return strings;
    }

    /// One parameter of a sweep, with its values in order.
    ///
    /// @param name the parameter's name
    /// @param values its values
    private record Parameter(String name, List<String> values) {

        private Parameter {
            values = List.copyOf(values);
        }

        @Override
        public String toString() {
            return name + "=" + values;
        }
    }
}
