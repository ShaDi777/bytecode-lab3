package org.example.analyzer;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.example.analyzer.jar.JarScanner;
import org.example.analyzer.metrics.MetricsService;
import org.example.analyzer.model.AnalysisResult;
import org.example.analyzer.report.ConsoleReporter;
import org.example.analyzer.report.JsonReporter;
import org.example.analyzer.report.Reporter;

public final class Main {

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        // args
        CliOptions options;
        try {
            options = CliOptions.parse(args);
        } catch (IllegalArgumentException e) {
            err.println("Error: " + e.getMessage());
            err.println(CliOptions.USAGE);
            return 2;
        }

        // path check
        Path jar = options.jar();
        if (!Files.isRegularFile(jar)) {
            err.println("Error: file not found: " + jar);
            return 1;
        }

        // analysis
        AnalysisResult result;
        try {
            result = new MetricsService().compute(jar, JarScanner.scan(jar, err::println));
        } catch (IOException e) {
            err.println("Error: failed to read jar: " + jar);
            err.println(e.getMessage());
            return 1;
        }

        // output
        Reporter consoleReporter = new ConsoleReporter();
        Reporter jsonReporter = new JsonReporter();
        consoleReporter.report(result, out);
        Path report = options.output();
        // a report file is just a PrintStream, so both reporters share one interface
        try (PrintStream reportStream = new PrintStream(Files.newOutputStream(report), false, StandardCharsets.UTF_8)) {
            jsonReporter.report(result, reportStream);
            reportStream.flush();
            if (reportStream.checkError()) {
                err.println("Error: failed to write json: " + report);
                return 1;
            }
        } catch (IOException e) {
            err.println("Error: failed to write json: " + e.getMessage());
            return 1;
        }
        out.println("json saved to: " + report.toAbsolutePath());

        return 0;
    }

    private Main() {
    }
}
