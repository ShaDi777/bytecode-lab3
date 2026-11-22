package org.example.analyzer.metrics;

import org.example.analyzer.TestJar;
import org.example.analyzer.model.ClassInfo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HierarchyCalculatorTest extends MetricsTestBase {

    @Test
    void depthCountsInJarAncestors() throws Exception {
        List<ClassInfo> types = TestJar.scan(TestJar.build(tmp, "h.jar", Map.of(
                "p.A", "package p; public class A { }",
                "p.B", "package p; public class B extends A { }",
                "p.C", "package p; public class C extends B { }",
                "p.D", "package p; public class D { }")));
        HierarchyCalculator hierarchy = new HierarchyCalculator(types);
        assertEquals(0, hierarchy.depthOf(TestJar.single(types, "p/A")));
        assertEquals(1, hierarchy.depthOf(TestJar.single(types, "p/B")));
        assertEquals(2, hierarchy.depthOf(TestJar.single(types, "p/C")));
        assertEquals(0, hierarchy.depthOf(TestJar.single(types, "p/D")));
    }

    @Test
    void parentOutsideJarIsRoot() throws Exception {
        List<ClassInfo> types = TestJar.scan(TestJar.build(tmp, "h.jar", Map.of("p.MyList", """
                package p;
                public class MyList extends java.util.AbstractList<String> {
                    public String get(int i) { return null; }
                    public int size() { return 0; }
                }
                """)));
        assertEquals(0, new HierarchyCalculator(types).depthOf(TestJar.single(types, "p/MyList")));
    }

    @Test
    void interfacesDoNotAddDepth() throws Exception {
        List<ClassInfo> types = TestJar.scan(TestJar.build(tmp, "h.jar", Map.of(
                "p.I", "package p; public interface I { }",
                "p.C", "package p; public class C implements I { }")));
        assertEquals(0, new HierarchyCalculator(types).depthOf(TestJar.single(types, "p/C")));
    }

    @Test
    void enumAndRecordAreRoots() throws Exception {
        List<ClassInfo> types = TestJar.scan(TestJar.build(tmp, "h.jar", Map.of(
                "p.E", "package p; public enum E { ONE, TWO }",
                "p.R", "package p; public record R(int x) { }")));
        HierarchyCalculator hierarchy = new HierarchyCalculator(types);
        assertEquals(0, hierarchy.depthOf(TestJar.single(types, "p/E")));
        assertEquals(0, hierarchy.depthOf(TestJar.single(types, "p/R")));
    }

    @Test
    void sharedFixtureDepths() throws Exception {
        List<ClassInfo> types = typesOf(sharedFixture());
        HierarchyCalculator hierarchy = new HierarchyCalculator(types);
        assertEquals(0, hierarchy.depthOf(TestJar.single(types, "p/Base")));
        assertEquals(1, hierarchy.depthOf(TestJar.single(types, "p/Sub")));
    }
}
