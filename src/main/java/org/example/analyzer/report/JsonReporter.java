package org.example.analyzer.report;

import java.io.PrintStream;
import java.util.Locale;
import org.example.analyzer.model.AbcCounters;
import org.example.analyzer.model.AnalysisResult;
import org.example.analyzer.model.ClassMetrics;
import org.example.analyzer.model.ProjectMetrics;

public final class JsonReporter implements Reporter {

    @Override
    public void report(AnalysisResult result, PrintStream out) {
        ProjectMetrics project = result.project();

        JsonBuilder json = new JsonBuilder().object(root -> root
            .key("jar").value(result.jar().toString())
            .key("classCount").value(project.classCount())
            .key("interfaceCount").value(project.interfaceCount())

            .key("project").object(section -> section
                .key("maxInheritanceDepth").value(project.maxInheritanceDepth())
                .key("avgInheritanceDepth").value(round(project.avgInheritanceDepth(), 3))
                .key("avgOverriddenMethods").value(round(project.avgOverriddenMethods(), 3))
                .key("avgFieldsPerClass").value(round(project.avgFieldsPerClass(), 3))
                .also(s -> writeAbc(s, project.abc())))

            .key("classes").array(entries -> result.types().forEach(
                metrics -> writeClass(entries, metrics))));

        out.print(json);
        out.println();
    }

    private static void writeClass(JsonBuilder json, ClassMetrics metrics) {
        json.object(entry -> entry
            .key("name").value(metrics.info().name().replace('/', '.'))
            .key("kind").value(metrics.info().kind().toString().toLowerCase(Locale.ROOT))
            .key("inheritanceDepth").value(metrics.depth())
            .key("fields").value(metrics.info().fieldCount())
            .key("overriddenMethods").value(metrics.overriddenMethods())
            .also(e -> writeAbc(e, metrics.info().abc())));
    }

    private static void writeAbc(JsonBuilder json, AbcCounters abc) {
        json.key("abc").object(a -> a
            .key("assignments").value(abc.assignments())
            .key("branches").value(abc.branches())
            .key("conditions").value(abc.conditions())
            .key("value").value(round(abc.abcValue(), 2)));
    }

    private static double round(double value, int places) {
        double factor = Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }
}
