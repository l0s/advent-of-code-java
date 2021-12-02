package com.macasaet;

import java.io.*;
import java.util.Objects;
import java.util.Properties;
import java.util.Spliterator;
import java.util.function.Consumer;

public class LineSpliterator implements Spliterator<String>, AutoCloseable {

    private static final String prefix;
    private final BufferedReader reader;

    static {
        final var properties = new Properties();
        try {
            final var config = LineSpliterator.class.getResourceAsStream("/config.properties");
            if (config != null) properties.load(config);
        } catch (final IOException ignored) {
        }
        prefix = properties.getProperty("prefix", "/sample");
    }

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

    public LineSpliterator(final String fileName) {
        this(LineSpliterator.class.getResourceAsStream(prefix + "/" + fileName));
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