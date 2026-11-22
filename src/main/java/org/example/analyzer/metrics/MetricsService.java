package org.example.analyzer.metrics;

import java.nio.file.Path;
import java.util.List;
import org.example.analyzer.model.AbcCounters;
import org.example.analyzer.model.AnalysisResult;
import org.example.analyzer.model.ClassInfo;
import org.example.analyzer.model.ClassMetrics;
import org.example.analyzer.model.ProjectMetrics;


public final class MetricsService {

    public AnalysisResult compute(Path jar, List<ClassInfo> types) {
        HierarchyCalculator hierarchy = new HierarchyCalculator(types);
        OverrideCalculator overrides = new OverrideCalculator(types);

        List<ClassMetrics> typeMetrics = types.stream()
                                              .map(type -> calculateTypeMetrics(type, hierarchy, overrides))
                                              .toList();

        ProjectMetrics projectMetrics = calculateProjectMetrics(typeMetrics);

        return new AnalysisResult(jar, typeMetrics, projectMetrics);
    }

    private ClassMetrics calculateTypeMetrics(
        ClassInfo type,
        HierarchyCalculator hierarchy,
        OverrideCalculator overrides
    ) {
        if (!type.kind().isClass()) {
            return new ClassMetrics(type, null, null);
        }

        return new ClassMetrics(
            type,
            hierarchy.depthOf(type),
            overrides.overriddenMethodCount(type)
        );
    }

    private ProjectMetrics calculateProjectMetrics(List<ClassMetrics> typeMetrics) {
        AbcCounters projectAbc = new AbcCounters();

        int classCount = 0;
        int interfaceCount = 0;
        int maxDepth = 0;

        long depthSum = 0;
        long overriddenSum = 0;
        long fieldsSum = 0;

        for (ClassMetrics metrics : typeMetrics) {
            ClassInfo type = metrics.info();
            projectAbc.add(type.abc());

            if (!type.kind().isClass()) {
                interfaceCount++;
                continue;
            }

            classCount++;
            maxDepth = Math.max(maxDepth, metrics.depth());
            depthSum += metrics.depth();
            overriddenSum += metrics.overriddenMethods();
            fieldsSum += type.fieldCount();
        }

        return new ProjectMetrics(
            classCount,
            interfaceCount,
            maxDepth,
            average(depthSum, classCount),
            average(overriddenSum, classCount),
            average(fieldsSum, classCount),
            projectAbc
        );
    }

    private static double average(long sum, int count) {
        return count == 0 ? 0.0 : (double) sum / count;
    }
}
