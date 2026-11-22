package org.example.analyzer.jar;

import org.example.analyzer.TestJar;
import org.example.analyzer.model.ClassInfo;
import org.example.analyzer.model.ClassKind;
import org.example.analyzer.model.MethodSig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassCollectorTest {

    @TempDir
    Path tmp;

    @Test
    void allDeclaredFieldsCount() throws Exception {
        ClassInfo f = single(Map.of("p.F", """
                package p;
                public class F {
                    int a;
                    static String b;
                    private final double c = 1.0;
                }
                """), "p/F");
        assertEquals(3, f.fieldCount());
    }

    @Test
    void compilerGeneratedFieldsCount() throws Exception {
        List<ClassInfo> types = TestJar.scan(TestJar.build(tmp, "c.jar", Map.of("p.Outer", """
                package p;
                public class Outer {
                    private int x;
                    class Inner {
                        int y;
                        int useOuter() { return x; }
                    }
                }
                """)));
        assertEquals(1, TestJar.single(types, "p/Outer").fieldCount());
        assertEquals(2, TestJar.single(types, "p/Outer$Inner").fieldCount());
    }

    @Test
    void enumConstantsAndValuesArrayCount() throws Exception {
        ClassInfo e = single(Map.of("p.E", "package p; public enum E { ONE, TWO }"), "p/E");
        // ONE, TWO, values[]
        assertEquals(3, e.fieldCount());
    }

    @Test
    void recordComponentsCount() throws Exception {
        ClassInfo r = single(Map.of("p.R", "package p; public record R(int x, String s) { }"), "p/R");
        assertEquals(2, r.fieldCount());
    }

    @Test
    void kindsAreDetected() throws Exception {
        List<ClassInfo> types = TestJar.scan(TestJar.build(tmp, "c.jar", Map.of(
                "p.Cls", "package p; public class Cls { }",
                "p.Iface", "package p; public interface Iface { }",
                "p.Anno", "package p; public @interface Anno { }",
                "p.E", "package p; public enum E { ONE }",
                "p.R", "package p; public record R(int x) { }")));
        assertEquals(ClassKind.CLASS, TestJar.single(types, "p/Cls").kind());
        assertEquals(ClassKind.INTERFACE, TestJar.single(types, "p/Iface").kind());
        assertEquals(ClassKind.ANNOTATION, TestJar.single(types, "p/Anno").kind());
        assertEquals(ClassKind.ENUM, TestJar.single(types, "p/E").kind());
        assertEquals(ClassKind.RECORD, TestJar.single(types, "p/R").kind());
    }

    @Test
    void declaredMethodsAreCollectedWithDescriptors() throws Exception {
        List<MethodSig> methods = single(Map.of("p.M", """
                package p;
                public class M {
                    void m() { }
                    void m(int i) { }
                    static void s() { }
                    private void p() { }
                    M() { }
                }
                """), "p/M").methods();
        assertEquals(5, methods.size());
        assertTrue(methods.contains(new MethodSig("m", "()V", 0)));
        assertTrue(methods.contains(new MethodSig("m", "(I)V", 0)));
        assertTrue(methods.stream().anyMatch(m -> m.name().equals("<init>") && m.descriptor().equals("()V")));
    }

    private ClassInfo single(Map<String, String> sources, String internalName) throws Exception {
        Path jar = TestJar.build(tmp, "c.jar", sources);
        return TestJar.single(TestJar.scan(jar), internalName);
    }
}
