package io.github.bineq.daleq.factextraction;

/**
 * Represents a single fact.
 * @author jens dietrich
 */
public record SimpleFact(Predicate predicate, String... values) implements Fact {
    @Override
    public String[] terms() {
        return values();
    }
}
