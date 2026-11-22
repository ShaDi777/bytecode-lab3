package org.example.analyzer;

import java.nio.file.Path;
import java.nio.file.Paths;

record CliOptions(Path jar, Path output) {

    static final String USAGE = """
            Usage: analyzer <input.jar> [-o <output.json>]
              <input.jar>   jar file to analyze
              -o <file>     json report path (default: <input_name>.report.json)
            """;

    static CliOptions parse(String[] args) {
        if (args.length != 1 && args.length != 3) {
            throw new IllegalArgumentException("expected 1 or 3 arguments, got " + args.length);
        }
        Path jar = Paths.get(args[0]);
        if (args.length == 3) {
            if (!"-o".equals(args[1])) {
                throw new IllegalArgumentException("unknown option: " + args[1]);
            }
            return new CliOptions(jar, Paths.get(args[2]));
        }
        return new CliOptions(jar, defaultOutput(jar));
    }

    static Path defaultOutput(Path jar) {
        String name = jar.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        Path parent = jar.toAbsolutePath().getParent();
        return parent == null ? Paths.get(base + ".report.json") : parent.resolve(base + ".report.json");
    }
}
