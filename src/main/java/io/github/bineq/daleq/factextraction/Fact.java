package io.github.bineq.daleq.factextraction;

public interface Fact {
    Predicate predicate();

    Object[] values();

    String asSouffleFact();
}
