package org.example.analyzer.model;

import java.util.List;

public record ClassInfo(
    String name,
    ClassKind kind,
    String superName,
    List<String> interfaces,
    List<MethodSig> methods,
    int fieldCount,
    AbcCounters abc
) {
}
