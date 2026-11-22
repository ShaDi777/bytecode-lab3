package org.example.analyzer;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import org.example.analyzer.jar.JarScanner;
import org.example.analyzer.model.ClassInfo;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Builds small test JARs: the fixture sources are compiled in-memory-adjacent (into a temp
 * directory) with the JDK compiler and packed into a JAR, so every test exercises the real
 * bytecode of the exact construct it describes.
 */
public final class TestJar {

    private TestJar() {
    }

    public static Path build(Path folder, String jarName, Map<String, String> sources) throws IOException {
        Path srcDir = Files.createDirectories(folder.resolve("src"));
        List<Path> files = new ArrayList<>();
        for (Map.Entry<String, String> source : sources.entrySet()) {
            Path file = srcDir.resolve(source.getKey().replace('.', '/') + ".java");
            Files.createDirectories(file.getParent());
            Files.writeString(file, source.getValue());
            files.add(file);
        }

        Path outDir = Files.createDirectories(folder.resolve("out"));
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            fail("fixture compilation needs a full JDK (javax.tools compiler not available)");
        }
        ByteArrayOutputStream compilerOutput = new ByteArrayOutputStream();
        String[] arguments = Stream.concat(
                        Stream.of("-d", outDir.toString()),
                        files.stream().map(Path::toString))
                .toArray(String[]::new);
        if (compiler.run(null, compilerOutput, compilerOutput, arguments) != 0) {
            fail("fixture compilation failed:\n" + compilerOutput);
        }

        Path jar = folder.resolve(jarName);
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar));
             Stream<Path> compiled = Files.walk(outDir)) {
            for (Path classFile : compiled.filter(Files::isRegularFile).toList()) {
                String entryName = outDir.relativize(classFile).toString().replace('\\', '/');
                output.putNextEntry(new ZipEntry(entryName));
                Files.copy(classFile, output);
                output.closeEntry();
            }
        }
        return jar;
    }

    public static List<ClassInfo> scan(Path jar) throws IOException {
        return JarScanner.scan(jar, warning -> fail("unexpected warning: " + warning));
    }

    public static ClassInfo single(List<ClassInfo> types, String internalName) {
        return types.stream()
                .filter(type -> type.name().equals(internalName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("class not found: " + internalName));
    }
}
