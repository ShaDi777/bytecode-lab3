package org.example.analyzer.model;

public record ClassMetrics(
    ClassInfo info,
    Integer depth,
    Integer overriddenMethods
) {
}
