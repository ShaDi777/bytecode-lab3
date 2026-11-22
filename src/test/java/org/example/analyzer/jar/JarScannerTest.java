package org.example.analyzer.jar;

import org.example.analyzer.TestJar;
import org.example.analyzer.model.ClassInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JarScannerTest {

    @TempDir
    Path tmp;

    @Test
    void infoClassesAreSkipped() throws Exception {
        Path jar = TestJar.build(tmp, "s.jar", Map.of(
                "module-info", "module info { }",
                "p.package-info", "package p;",
                "p.C", "package p; public class C { }"));
        List<ClassInfo> types = TestJar.scan(jar);
        assertEquals(1, types.size());
        assertEquals("p/C", types.get(0).name());
    }

    @Test
    void brokenClassEntryIsSkippedWithWarning() throws Exception {
        Path jar = tmp.resolve("broken.jar");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(jar))) {
            zip.putNextEntry(new ZipEntry("p/A.class"));
            zip.write("not a bytecode".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }

        List<String> warnings = new ArrayList<>();
        List<ClassInfo> types = JarScanner.scan(jar, warnings::add);

        assertTrue(types.isEmpty());
        assertEquals(1, warnings.size());
    }

    @Test
    void missingJarIsReported() throws IOException {
        Path missing = tmp.resolve("missing.jar");
        List<String> warnings = new ArrayList<>();
        try {
            JarScanner.scan(missing, warnings::add);
        } catch (IOException expected) {
            return;
        }
        throw new AssertionError("scanning a missing JAR must throw");
    }
}
