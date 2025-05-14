package io.github.bineq.daleq.ui;

/**
 * Result of an analysis.
 * @author jens dietrich
 */
public record AnalysisResult(AnalysisResultState state, String message) {}
