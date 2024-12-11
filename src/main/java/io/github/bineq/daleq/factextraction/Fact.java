package io.github.bineq.daleq.factextraction;

public interface Fact {
    Predicate predicate();

    String[] terms();
}
