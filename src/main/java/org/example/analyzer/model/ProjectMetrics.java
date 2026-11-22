package org.example.analyzer.model;

public record ProjectMetrics(
    int classCount,
    int interfaceCount,
    int maxInheritanceDepth,
    double avgInheritanceDepth,
    double avgOverriddenMethods,
    double avgFieldsPerClass,
    AbcCounters abc
) {
}
