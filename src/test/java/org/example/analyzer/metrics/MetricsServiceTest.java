package org.example.analyzer.metrics;

import org.example.analyzer.model.AnalysisResult;
import org.example.analyzer.model.ClassKind;
import org.example.analyzer.model.ClassMetrics;
import org.example.analyzer.model.ProjectMetrics;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MetricsServiceTest extends MetricsTestBase {

    @Test
    void projectMetricsAreExact() throws Exception {
        ProjectMetrics project = sharedFixture().project();
        assertEquals(2, project.classCount());
        assertEquals(2, project.interfaceCount());
        assertEquals(1, project.maxInheritanceDepth());
        assertEquals(0.5, project.avgInheritanceDepth(), 1e-9);
        assertEquals(1.0, project.avgOverriddenMethods(), 1e-9);
        assertEquals(1.5, project.avgFieldsPerClass(), 1e-9);
        assertEquals(1, project.abc().assignments());
        assertEquals(2, project.abc().branches());
        assertEquals(1, project.abc().conditions());
        assertEquals(Math.sqrt(6), project.abc().abcValue(), 1e-9);
    }

    @Test
    void classMetricsAreExact() throws Exception {
        Map<String, ClassMetrics> byName = metricsByType(sharedFixture());

        ClassMetrics base = byName.get("p/Base");
        assertEquals(ClassKind.CLASS, base.info().kind());
        assertEquals("java/lang/Object", base.info().superName());
        assertEquals(List.of("p/I"), base.info().interfaces());
        assertEquals(4, base.info().methods().size());
        assertEquals(1, base.info().fieldCount());
        assertEquals(0, base.depth());
        assertEquals(1, base.overriddenMethods());
        assertEquals(1, base.info().abc().assignments());
        assertEquals(1, base.info().abc().branches());
        assertEquals(1, base.info().abc().conditions());
        assertEquals(Math.sqrt(3), base.info().abc().abcValue(), 1e-9);

        ClassMetrics sub = byName.get("p/Sub");
        assertEquals(ClassKind.CLASS, sub.info().kind());
        assertEquals("p/Base", sub.info().superName());
        assertEquals(List.of("p/I"), sub.info().interfaces());
        assertEquals(2, sub.info().fieldCount());
        assertEquals(1, sub.depth());
        assertEquals(1, sub.overriddenMethods());
        assertEquals(0, sub.info().abc().assignments());
        assertEquals(1, sub.info().abc().branches());
        assertEquals(0, sub.info().abc().conditions());

        ClassMetrics iface = byName.get("p/I");
        assertEquals(ClassKind.INTERFACE, iface.info().kind());
        assertEquals(2, iface.info().methods().size());
        assertEquals(0, iface.info().fieldCount());
        assertNull(iface.depth());
        assertNull(iface.overriddenMethods());

        ClassMetrics annotation = byName.get("p/Ann");
        assertEquals(ClassKind.ANNOTATION, annotation.info().kind());
        assertEquals(0, annotation.info().methods().size());
        assertNull(annotation.depth());
        assertNull(annotation.overriddenMethods());
    }

    @Test
    void projectAbcSumsPerTypeComponents() throws Exception {
        AnalysisResult result = sharedFixture();
        long assignments = result.types().stream().mapToLong(m -> m.info().abc().assignments()).sum();
        long branches = result.types().stream().mapToLong(m -> m.info().abc().branches()).sum();
        long conditions = result.types().stream().mapToLong(m -> m.info().abc().conditions()).sum();
        assertEquals(result.project().abc().assignments(), assignments);
        assertEquals(result.project().abc().branches(), branches);
        assertEquals(result.project().abc().conditions(), conditions);
    }

    @Test
    void jarWithoutClassesHasZeroAverages() throws Exception {
        ProjectMetrics project = analyze(Map.of(
                "p.I", "package p; public interface I { void x(); }")).project();
        assertEquals(0, project.classCount());
        assertEquals(1, project.interfaceCount());
        assertEquals(0, project.maxInheritanceDepth());
        assertEquals(0.0, project.avgInheritanceDepth(), 1e-9);
        assertEquals(0.0, project.avgOverriddenMethods(), 1e-9);
        assertEquals(0.0, project.avgFieldsPerClass(), 1e-9);
    }

    @Test
    void interfaceFieldsAreNotAveraged() throws Exception {
        ProjectMetrics project = analyze(Map.of(
                "p.I", "package p; public interface I { int CONST = 1; }",
                "p.One", "package p; public class One implements I { }",
                "p.Two", "package p; public class Two { int a; int b; int c; }")).project();
        assertEquals(2, project.classCount());
        assertEquals(1, project.interfaceCount());
        assertEquals(1.5, project.avgFieldsPerClass(), 1e-9);
    }

    @Test
    void abstractClassesParticipate() throws Exception {
        ProjectMetrics project = analyze(Map.of(
                "p.Abs", "package p; public abstract class Abs { int a; }",
                "p.Con", "package p; public class Con extends Abs { int b; int c; }")).project();
        assertEquals(2, project.classCount());
        assertEquals(1, project.maxInheritanceDepth());
        assertEquals(1.5, project.avgFieldsPerClass(), 1e-9);
    }
}
