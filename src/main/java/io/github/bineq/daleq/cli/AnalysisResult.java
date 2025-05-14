package io.github.bineq.daleq.cli;

/**
 * Result of an analysis.
 * @author jens dietrich
 */
public record AnalysisResult(AnalysisResultState state, String message) {}
