package io.github.bineq.daleq.evaluation;

/**
 * The result of comparing two binaries.
 * @author jens dietrich
 */
public enum ComparisonResult {

    // note that the order is used in comparisons based on compareTo, so this matters !
    EQUAL, EQUIVALENT, NON_EQUIVALENT, ERROR
}
