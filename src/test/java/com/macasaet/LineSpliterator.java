package com.macasaet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

public class LineSpliterator implements Spliterator<String>, AutoCloseable {

    private final BufferedReader reader;

    public LineSpliterator(final BufferedReader reader) {
        Objects.requireNonNull(reader);
        this.reader = reader;
    }

    public LineSpliterator(final Reader reader) {
        this(new BufferedReader(reader));
    }

    public LineSpliterator(final InputStream stream) {
        this(new InputStreamReader(stream));
    }

    public boolean tryAdvance(final Consumer<? super String> action) {
        try {
            final var line = reader.readLine();
            if (line != null) {
                action.accept(line);
                return true;
            }
            reader.close();
        } catch (final IOException ioe) {
            ioe.printStackTrace();
        }
        return false;
    }

    public Spliterator<String> trySplit() {
        return null;
    }

    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    public int characteristics() {
        return ORDERED | NONNULL | IMMUTABLE;
    }

    public void close() throws IOException {
        reader.close();
    }

}