package io.github.bineq.daleq.factextraction;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a single fact.
 * @author jens dietrich
 */
public record SimpleFact(Predicate predicate, Object... values) implements Fact {


    @Override
    public String asSouffleFact() {
        return Stream.of(values)
            .map(value -> String.valueOf(value))
            .collect(Collectors.joining("\t"));
    }
}
