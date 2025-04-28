package io.github.bineq.daleq.evaluation;

import com.google.common.base.Preconditions;

import java.util.Objects;

/**
 * A pair of records for the same GAV.
 * @author jens dietrich
 */
public record PairOfRecords(Record left, Record right) {
    public PairOfRecords(Record left, Record right) {
        this.left = left;
        this.right = right;
        Preconditions.checkArgument(Objects.equals(this.left.gav(),this.right.gav()));
    }
}
