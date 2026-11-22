package org.example.analyzer.metrics;

import org.example.analyzer.TestJar;
import org.example.analyzer.model.ClassInfo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OverrideCalculatorTest extends MetricsTestBase {

    @Test
    void overrideCountsAndOverloadDoesNot() throws Exception {
        List<ClassInfo> types = TestJar.scan(TestJar.build(tmp, "o.jar", Map.of(
                "p.A", "package p; public class A { void m() { } void m(int i) { } }",
                "p.B", "package p; public class B extends A { void m() { } void m(int i) { } void m(String s) { } }")));
        assertEquals(2, new OverrideCalculator(types).overriddenMethodCount(TestJar.single(types, "p/B")));
    }

    @Test
    void staticAndPrivateMethodsAreIgnored() throws Exception {
        List<ClassInfo> types = TestJar.scan(TestJar.build(tmp, "o.jar", Map.of(
                "p.S", "package p; public class S { static void s() { } private void p() { } }",
                "p.T", "package p; public class T extends S { static void s() { } private void p() { } }")));
        assertEquals(0, new OverrideCalculator(types).overriddenMethodCount(TestJar.single(types, "p/T")));
    }

    @Test
    void interfaceMethodsAndDefaultMethodsCount() throws Exception {
        List<ClassInfo> types = TestJar.scan(TestJar.build(tmp, "o.jar", Map.of(
                "p.I", "package p; public interface I { void x(); default int y() { return 1; } }",
                "p.C", "package p; public class C implements I { public void x() { } public int y() { return 2; } }")));
        assertEquals(2, new OverrideCalculator(types).overriddenMethodCount(TestJar.single(types, "p/C")));
    }

    @Test
    void transitiveAndAncestorInterfacesCount() throws Exception {
        List<ClassInfo> types = TestJar.scan(TestJar.build(tmp, "o.jar", Map.of(
                "p.I1", "package p; public interface I1 { void a(); }",
                "p.I2", "package p; public interface I2 extends I1 { void b(); }",
                "p.D", "package p; public class D implements I2 { public void a() { } public void b() { } }",
                "p.Base", "package p; public class Base implements I2 { public void a() { } public void b() { } }",
                "p.Sub", "package p; public class Sub extends Base implements I1 { public void a() { } }")));
        OverrideCalculator overrides = new OverrideCalculator(types);
        assertEquals(2, overrides.overriddenMethodCount(TestJar.single(types, "p/D")));
        assertEquals(1, overrides.overriddenMethodCount(TestJar.single(types, "p/Sub")));
    }

    @Test
    void abstractAncestorMethodCounts() throws Exception {
        List<ClassInfo> types = TestJar.scan(TestJar.build(tmp, "o.jar", Map.of(
                "p.Ab", "package p; public abstract class Ab { abstract void m(); }",
                "p.Con", "package p; public class Con extends Ab { void m() { } }")));
        assertEquals(1, new OverrideCalculator(types).overriddenMethodCount(TestJar.single(types, "p/Con")));
    }

    @Test
    void bridgeMethodsAreExcluded() throws Exception {
        List<ClassInfo> types = TestJar.scan(TestJar.build(tmp, "o.jar", Map.of(
                "p.Gen", "package p; public class Gen<T> { T get() { return null; } }",
                "p.Spec", """
                        package p;
                        public class Spec extends Gen<String> {
                            @Override public String get() { return null; }
                        }
                        """)));
        assertEquals(0, new OverrideCalculator(types).overriddenMethodCount(TestJar.single(types, "p/Spec")));
    }

    @Test
    void ancestorsOutsideJarAreIgnored() throws Exception {
        List<ClassInfo> types = TestJar.scan(TestJar.build(tmp, "o.jar", Map.of(
                "p.MyList", """
                        package p;
                        public class MyList extends java.util.AbstractList<String> {
                            public boolean isEmpty() { return true; }
                            public String get(int i) { return null; }
                            public int size() { return 0; }
                        }
                        """)));
        assertEquals(0, new OverrideCalculator(types).overriddenMethodCount(TestJar.single(types, "p/MyList")));
    }

    @Test
    void toStringOverrideIsNotCounted() throws Exception {
        List<ClassInfo> types = TestJar.scan(TestJar.build(tmp, "o.jar", Map.of("p.C", """
                package p;
                public class C {
                    @Override public String toString() { return "c"; }
                }
                """)));
        assertEquals(0, new OverrideCalculator(types).overriddenMethodCount(TestJar.single(types, "p/C")));
    }

    @Test
    void sharedFixtureOverrides() throws Exception {
        List<ClassInfo> types = typesOf(sharedFixture());
        OverrideCalculator overrides = new OverrideCalculator(types);
        assertEquals(1, overrides.overriddenMethodCount(TestJar.single(types, "p/Base")));
        assertEquals(1, overrides.overriddenMethodCount(TestJar.single(types, "p/Sub")));
    }
}
