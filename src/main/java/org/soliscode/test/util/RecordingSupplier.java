package org.soliscode.test.util;

import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Supplier;

public interface RecordingSupplier<T> extends Supplier<T> {

    @NonNull List<T> recorded();
}
