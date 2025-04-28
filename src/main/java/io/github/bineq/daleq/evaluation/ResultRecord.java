package io.github.bineq.daleq.evaluation;

/**
 * Result of comparing two classes.
 * @author jens dietrich
 */
public record ResultRecord(String gav, String provider1, String provider2,String clazz, ComparisonResult result) {
}
