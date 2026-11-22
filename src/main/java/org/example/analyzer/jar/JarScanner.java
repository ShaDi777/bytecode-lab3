package org.example.analyzer.jar;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.example.analyzer.model.ClassInfo;
import org.objectweb.asm.ClassReader;

public final class JarScanner {

    private JarScanner() {
    }

    public static List<ClassInfo> scan(Path jar, Consumer<String> warning) throws IOException {
        List<ClassInfo> result = new ArrayList<>();
        try (JarFile file = new JarFile(jar.toFile())) {
            Enumeration<JarEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (shouldSkipEntry(entryName)) {
                    continue;
                }
                try {
                    ClassInfo info = readEntry(file, entry);
                    result.add(info);
                } catch (Exception e) {
                    warning.accept("warning: skipping entry " + entryName + ": " + e.getMessage());
                }
            }
        }
        result.sort(Comparator.comparing(ClassInfo::name));
        return result;
    }

    private static ClassInfo readEntry(JarFile file, JarEntry entry) throws IOException {
        ClassCollector collector = new ClassCollector();
        try (InputStream in = file.getInputStream(entry)) {
            new ClassReader(in).accept(collector, 0);
        }
        return collector.getResultInfo();
    }

    private static boolean shouldSkipEntry(String entryName) {
        if (!entryName.endsWith(".class") || entryName.startsWith("META-INF/")) {
            return true;
        }

        String simple = entryName.substring(entryName.lastIndexOf('/') + 1);
        return simple.equals("module-info.class") || simple.equals("package-info.class");
    }
}
