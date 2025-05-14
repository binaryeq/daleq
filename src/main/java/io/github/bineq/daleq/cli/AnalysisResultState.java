package io.github.bineq.daleq.cli;

/**
 * Analysis result states.
 * Those correspond to the states used in unit testing.
 * @author jens dietrich
 */
public enum AnalysisResultState {
    PASS,  // check passes
    FAIL,  // check fails
    ERROR, // error during check
    SKIP   // check preconditions not satisfied
}
