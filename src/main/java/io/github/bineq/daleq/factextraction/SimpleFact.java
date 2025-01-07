package io.github.bineq.daleq.factextraction;

import com.google.common.collect.Streams;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Represents a single fact.
 * @author jens dietrich
 */
public record SimpleFact(Predicate predicate, Object... values) implements Fact {

    @Override
    public String asSouffleFact() {
        assert predicate.getSlots().length==values.length;
        return IntStream.range(0, predicate.getSlots().length)
            .mapToObj(i -> {
                Slot slot = predicate.getSlots()[i];
                return stringify(values[i],slot);
            })
            .collect(Collectors.joining("\t"));
    }

    private String stringify(Object obj,Slot slot) {
        if (slot.jtype().endsWith("]")) {
            assert obj.getClass().isArray();
            Object[] array = (Object[]) obj;
            // nested arrays are not supported
            return Stream.of(array).map(v -> String.valueOf(v)).collect(Collectors.joining(","));
        }
        return String.valueOf(obj);
    }
}
