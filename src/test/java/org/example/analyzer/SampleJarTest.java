package org.example.analyzer;

import org.example.analyzer.metrics.MetricsService;
import org.example.analyzer.model.AnalysisResult;
import org.example.analyzer.model.ClassKind;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SampleJarTest {

    @Test
    void sampleJarMetrics() throws IOException, URISyntaxException {
        Path jar = Path.of(Objects.requireNonNull(
                SampleJarTest.class.getResource("/sample.jar"), "sample.jar must be on the classpath").toURI());
        AnalysisResult result = new MetricsService().compute(jar, TestJar.scan(jar));

        assertEquals(8, result.project().classCount());
        assertEquals(2, result.project().interfaceCount());
        assertEquals(1, result.project().maxInheritanceDepth());
        assertEquals(0.625, result.project().avgInheritanceDepth(), 1e-9);
        assertEquals(0.75, result.project().avgOverriddenMethods(), 1e-9);
        assertEquals(0.875, result.project().avgFieldsPerClass(), 1e-9);
        assertEquals(6, result.project().abc().assignments());
        assertEquals(35, result.project().abc().branches());
        assertEquals(7, result.project().abc().conditions());
        assertEquals(ClassKind.INTERFACE, kindOf(result, "Y"));
        assertEquals(ClassKind.CLASS, kindOf(result, "AKt"));
    }

    private static ClassKind kindOf(AnalysisResult result, String simpleName) {
        return result.types().stream()
                .map(m -> m.info())
                .filter(info -> info.name().equals(simpleName))
                .findFirst()
                .orElseThrow()
                .kind();
    }
}
