package org.example.analyzer.report;

import org.example.analyzer.model.AbcCounters;
import org.example.analyzer.model.AnalysisResult;
import org.example.analyzer.model.ClassInfo;
import org.example.analyzer.model.ClassMetrics;
import org.example.analyzer.model.ProjectMetrics;

import java.io.PrintStream;
import java.util.Locale;

public final class ConsoleReporter implements Reporter {

    @Override
    public void report(AnalysisResult result, PrintStream out) {
        ProjectMetrics project = result.project();
        out.printf(Locale.ROOT, "Analyzed JAR: %s%n", result.jar());
        out.printf(Locale.ROOT, "Classes: %d (incl. enums and records), interfaces: %d%n%n",
                project.classCount(), project.interfaceCount());

        out.println("Project metrics");
        out.printf(Locale.ROOT, "  Max inheritance depth:   %d%n", project.maxInheritanceDepth());
        out.printf(Locale.ROOT, "  Avg inheritance depth:   %.3f%n", project.avgInheritanceDepth());
        out.printf(Locale.ROOT, "  Avg overridden methods:  %.3f%n", project.avgOverriddenMethods());
        out.printf(Locale.ROOT, "  Avg fields per class:    %.3f%n", project.avgFieldsPerClass());
        out.printf(Locale.ROOT, "  Project ABC:             A=%d B=%d C=%d -> %.2f%n%n",
                project.abc().assignments(), project.abc().branches(),
                project.abc().conditions(), project.abc().abcValue());

        out.println("Per-type metrics");
        out.printf(Locale.ROOT, "  %-45s %-10s %5s %6s %11s %6s %6s %6s %9s%n",
                "Name", "Kind", "Depth", "Fields", "Overridden", "A", "B", "C", "ABC");
        for (ClassMetrics metrics : result.types()) {
            printType(metrics, out);
        }
    }

    private void printType(ClassMetrics metrics, PrintStream out) {
        ClassInfo type = metrics.info();
        AbcCounters abc = type.abc();
        out.printf(Locale.ROOT, "  %-45s %-10s %5s %6d %11s %6d %6d %6d %9.2f%n",
                type.name().replace('/', '.'),
                type.kind().toString().toLowerCase(Locale.ROOT),
                metrics.depth() == null ? "-" : metrics.depth(),
                type.fieldCount(),
                metrics.overriddenMethods() == null ? "-" : metrics.overriddenMethods(),
                abc.assignments(), abc.branches(), abc.conditions(), abc.abcValue());
    }
}
