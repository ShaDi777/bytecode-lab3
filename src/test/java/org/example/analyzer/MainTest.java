package org.example.analyzer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    @TempDir
    Path tmp;

    @Test
    void fullRunPrintsConsoleAndWritesJson() throws Exception {
        Path jar = TestJar.build(tmp, "app.jar", Map.of(
                "p.A", "package p; public class A { void m() { } }",
                "p.B", "package p; public class B extends A { void m() { } int f; }"));
        Path report = tmp.resolve("app.report.json");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int exit = Main.run(new String[]{jar.toString(), "-o", report.toString()},
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(ByteArrayOutputStream.nullOutputStream()));

        assertEquals(0, exit);
        String console = out.toString(StandardCharsets.UTF_8);
        assertTrue(console.contains("Project metrics"));
        assertTrue(console.contains("Avg inheritance depth"));
        assertTrue(console.contains("p.B"));

        String json = Files.readString(report);
        assertTrue(json.startsWith("{"));
        assertTrue(json.contains("\"project\""));
        assertTrue(json.contains("\"classes\""));
        assertTrue(json.contains("\"name\": \"p.B\""));
        assertTrue(json.contains("\"overriddenMethods\": 1"));
        assertTrue(json.contains("\"maxInheritanceDepth\": 1"));
    }

    @Test
    void defaultReportLandsNextToTheJar() throws Exception {
        Path jar = TestJar.build(tmp, "app.jar", Map.of(
                "p.A", "package p; public class A { }"));

        int exit = Main.run(new String[]{jar.toString()},
                new PrintStream(ByteArrayOutputStream.nullOutputStream()),
                new PrintStream(ByteArrayOutputStream.nullOutputStream()));

        assertEquals(0, exit);
        assertTrue(Files.isRegularFile(tmp.resolve("app.report.json")));
    }

    @Test
    void missingFileFails() {
        int exit = runCatching(tmp.resolve("nope.jar").toString());
        assertEquals(1, exit);
    }

    @Test
    void nonJarFileFails() throws Exception {
        Path notAJar = tmp.resolve("fake.jar");
        Files.writeString(notAJar, "this is not a zip archive");
        assertEquals(1, runCatching(notAJar.toString()));
    }

    @Test
    void unwritableReportPathFails() throws Exception {
        Path jar = TestJar.build(tmp, "app.jar", Map.of(
                "p.A", "package p; public class A { }"));
        int exit = Main.run(new String[]{jar.toString(), "-o", tmp.toString()},
                new PrintStream(ByteArrayOutputStream.nullOutputStream()),
                new PrintStream(ByteArrayOutputStream.nullOutputStream()));
        assertEquals(1, exit);
    }

    @Test
    void badArgumentsFailWithUsage() {
        for (String[] args : new String[][]{{}, {"a.jar", "-x", "b"}}) {
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            int exit = Main.run(args,
                    new PrintStream(ByteArrayOutputStream.nullOutputStream()),
                    new PrintStream(err, true, StandardCharsets.UTF_8));
            assertEquals(2, exit);
            assertTrue(err.toString(StandardCharsets.UTF_8).contains("Usage:"));
        }
    }

    private int runCatching(String jar) {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        return Main.run(new String[]{jar},
                new PrintStream(ByteArrayOutputStream.nullOutputStream()),
                new PrintStream(err, true, StandardCharsets.UTF_8));
    }
}
