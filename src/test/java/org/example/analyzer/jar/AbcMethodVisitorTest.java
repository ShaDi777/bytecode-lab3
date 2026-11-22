package org.example.analyzer.jar;

import org.example.analyzer.TestJar;
import org.example.analyzer.model.ClassInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbcMethodVisitorTest {

    @TempDir
    Path tmp;

    @Test
    void storeInstructionsCountAsAssignments() throws Exception {
        ClassInfo a = fixture("""
                package p;
                public class A {
                    public void stores(int i, long l, float f, double d, Object o) {
                        int a = i; long b = l; float c = f; double e = d; Object g = o;
                    }
                }
                """);
        assertEquals(5, a.abc().assignments());
        assertEquals(0, a.abc().conditions());
    }

    @Test
    void iincIsNotAnAssignment() throws Exception {
        ClassInfo a = fixture("""
                package p;
                public class A {
                    public int inc(int x) {
                        x++; x += 2; return x;
                    }
                }
                """);
        assertEquals(0, a.abc().assignments());
    }

    @Test
    void invocationsCountAsBranches() throws Exception {
        ClassInfo a = fixture("""
                package p;
                public class A {
                    public int calls() {
                        return String.valueOf(1).length() + Math.max(1, 2);
                    }
                }
                """);
        // three invocations in calls() + the implicit super() call of the constructor
        assertEquals(4, a.abc().branches());
        assertEquals(0, a.abc().assignments());
        assertEquals(0, a.abc().conditions());
    }

    @Test
    void invokedynamicLambdaCountsAsBranch() throws Exception {
        ClassInfo a = fixture("""
                package p;
                public class A {
                    public Runnable lambda() {
                        return () -> { };
                    }
                }
                """);
        assertEquals(2, a.abc().branches());
        assertEquals(0, a.abc().assignments());
    }

    @Test
    void conditionalJumpsCountAsConditions() throws Exception {
        ClassInfo a = fixture("""
                package p;
                public class A {
                    public int cond(int x) {
                        if (x > 0) {
                            return 1;
                        }
                        return x == 5 ? 2 : 3;
                    }
                }
                """);
        assertEquals(2, a.abc().conditions());
    }

    @Test
    void tableSwitchCountsAsSingleCondition() throws Exception {
        ClassInfo a = fixture("""
                package p;
                public class A {
                    public int table(int x) {
                        switch (x) { case 1: return 1; case 2: return 2; case 3: return 3; default: return 0; }
                    }
                }
                """);
        assertEquals(1, a.abc().conditions());
    }

    @Test
    void lookupSwitchCountsAsSingleCondition() throws Exception {
        ClassInfo a = fixture("""
                package p;
                public class A {
                    public int lookup(int x) {
                        switch (x) { case 1: return 1; case 1000000: return 2; default: return 0; }
                    }
                }
                """);
        assertEquals(1, a.abc().conditions());
    }

    @Test
    void valueFollowsFormula() throws Exception {
        ClassInfo a = fixture("""
                package p;
                public class A {
                    public int combined(int x) {
                        int a = x;
                        if (a > 0) { a = a + 1; }
                        return Math.max(a, 0);
                    }
                }
                """);
        assertEquals(2, a.abc().assignments());
        assertEquals(2, a.abc().branches());
        assertEquals(1, a.abc().conditions());
        assertEquals(Math.sqrt(9), a.abc().abcValue(), 1e-9);
    }

    @Test
    void constructorsAndStaticInitializersAreIncluded() throws Exception {
        ClassInfo a = fixture("""
                package p;
                public class A {
                    static int counter = compute();
                    A(int x) { counter += x; }
                    static int compute() { return 1; }
                }
                """);
        assertEquals(0, a.abc().assignments());
        assertEquals(2, a.abc().branches());
    }

    private ClassInfo fixture(String source) throws Exception {
        Path jar = TestJar.build(tmp, "abc.jar", Map.of("p.A", source));
        return TestJar.single(TestJar.scan(jar), "p/A");
    }
}
