package org.example.analyzer.report;

import org.example.analyzer.model.AnalysisResult;

import java.io.PrintStream;

public interface Reporter {

    void report(AnalysisResult result, PrintStream out);
}
