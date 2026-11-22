package org.example.analyzer.model;

import java.nio.file.Path;
import java.util.List;

public record AnalysisResult(
    Path jar,
    List<ClassMetrics> types,
    ProjectMetrics project
) {
}
