package org.example.analyzer.metrics;

import org.example.analyzer.TestJar;
import org.example.analyzer.jar.JarScanner;
import org.example.analyzer.model.AnalysisResult;
import org.example.analyzer.model.ClassInfo;
import org.example.analyzer.model.ClassMetrics;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common infrastructure for metric tests: temporary storage, one shared fixture JAR with
 * fully hand-computable metrics, and small helpers to analyze ad-hoc fixtures.
 */
abstract class MetricsTestBase {

    @TempDir
    Path tmp;

    private AnalysisResult sharedFixture;

    protected AnalysisResult sharedFixture() throws IOException {
        if (sharedFixture == null) {
            sharedFixture = analyze(Map.of(
                    "p.I", "package p; public interface I { void x(); default int y() { return 1; } }",
                    "p.Ann", "package p; public @interface Ann { }",
                    "p.Base", """
                            package p;
                            public class Base implements I {
                                int one;
                                public void x() { }
                                public int keep(int v) { int kept = v; return kept; }
                                public boolean flag(int v) { return v > 0; }
                            }""",
                    "p.Sub", """
                            package p;
                            public class Sub extends Base implements I {
                                int two;
                                int three;
                                @Override public int y() { return 2; }
                            }"""));
        }
        return sharedFixture;
    }

    protected AnalysisResult analyze(Map<String, String> sources) throws IOException {
        Path jar = TestJar.build(tmp, "fixture.jar", sources);
        return new MetricsService().compute(jar, JarScanner.scan(jar, warning -> { }));
    }

    protected static List<ClassInfo> typesOf(AnalysisResult result) {
        return result.types().stream().map(ClassMetrics::info).toList();
    }

    protected static Map<String, ClassMetrics> metricsByType(AnalysisResult result) {
        Map<String, ClassMetrics> byName = new HashMap<>();
        for (ClassMetrics metrics : result.types()) {
            byName.put(metrics.info().name(), metrics);
        }
        return byName;
    }
}
