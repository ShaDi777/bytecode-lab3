package org.example.analyzer.report;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

final class JsonBuilder {

    private final StringBuilder sb = new StringBuilder();
    private final Deque<Boolean> elementWritten = new ArrayDeque<>();
    private int indent;
    private boolean afterKey;

    JsonBuilder beginObject() {
        elementPrefix();
        sb.append('{');
        indent++;
        elementWritten.push(false);
        return this;
    }

    JsonBuilder endObject() {
        boolean empty = !elementWritten.pop();
        indent--;
        if (!empty) {
            newlineIndent();
        }
        sb.append('}');
        afterKey = false;
        return this;
    }

    JsonBuilder beginArray() {
        elementPrefix();
        sb.append('[');
        indent++;
        elementWritten.push(false);
        return this;
    }

    JsonBuilder endArray() {
        boolean empty = !elementWritten.pop();
        indent--;
        if (!empty) {
            newlineIndent();
        }
        sb.append(']');
        afterKey = false;
        return this;
    }

    JsonBuilder key(String name) {
        elementPrefix();
        sb.append(quote(name)).append(": ");
        afterKey = true;
        return this;
    }

    JsonBuilder value(String value) {
        elementPrefix();
        sb.append(quote(value));
        return this;
    }

    JsonBuilder value(long value) {
        elementPrefix();
        sb.append(value);
        return this;
    }

    JsonBuilder value(double value) {
        elementPrefix();
        sb.append(BigDecimal.valueOf(value).stripTrailingZeros().toPlainString());
        return this;
    }

    JsonBuilder value(Number value) {
        return value == null ? nullValue() : value(value.longValue());
    }

    JsonBuilder nullValue() {
        elementPrefix();
        sb.append("null");
        return this;
    }

    JsonBuilder object(Consumer<JsonBuilder> body) {
        beginObject();
        body.accept(this);
        return endObject();
    }

    JsonBuilder array(Consumer<JsonBuilder> body) {
        beginArray();
        body.accept(this);
        return endArray();
    }

    JsonBuilder also(Consumer<JsonBuilder> modification) {
        modification.accept(this);
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    private void elementPrefix() {
        if (afterKey) {
            afterKey = false;
            return;
        }
        Boolean written = elementWritten.peek();
        if (written == null) {
            return;
        }
        if (written) {
            sb.append(',');
        }
        elementWritten.pop();
        elementWritten.push(true);
        newlineIndent();
    }

    private void newlineIndent() {
        sb.append('\n');
        sb.append("  ".repeat(indent));
    }

    private static String quote(String value) {
        StringBuilder quoted = new StringBuilder(value.length() + 2);
        quoted.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> quoted.append("\\\"");
                case '\\' -> quoted.append("\\\\");
                case '\n' -> quoted.append("\\n");
                case '\r' -> quoted.append("\\r");
                case '\t' -> quoted.append("\\t");
                default -> quoted.append(c);
            }
        }
        quoted.append('"');
        return quoted.toString();
    }
}
