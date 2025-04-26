package io.github.bineq.daleq;

import java.util.Arrays;
import java.util.Objects;
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
                //  do we need to sanitise string literals for CSV ?
                boolean sanitizeValue4CSV = predicate.getName().equals("LDC") && slot.name().equals("cst");
                return stringify(values[i],slot,sanitizeValue4CSV);
            })
            .collect(Collectors.joining("\t"));
    }

    private String stringify(Object obj,Slot slot,boolean sanitizeValue4CSV) {
        if (sanitizeValue4CSV) {
            return String.valueOf(obj)
                .replace("\t","\\t")
                .replace("\n","\\n")
                .replace("\r","\\r");
        }
        else if (slot.jtype().endsWith("]")) {
            assert obj.getClass().isArray();
            Object[] array = (Object[]) obj;
            // nested arrays are not supported
            return Stream.of(array).map(v -> String.valueOf(v)).collect(Collectors.joining(","));
        }
        else if (slot.jtype().equals("boolean")) {
            if (obj==Boolean.TRUE) {
                return "1";
            }
            else if (obj==Boolean.FALSE) {
                return "0";
            }
            assert false;
            return String.valueOf(obj);
        }
        else {
            return String.valueOf(obj);
        }
    }

    @Override
    public String toString() {
        return IntStream.range(0, predicate.getSlots().length)
            .mapToObj(i -> predicate.getSlots()[i].name() + "=" + this.values[i])
            .collect(Collectors.joining(", ",this.predicate.getName()+"[","]"));
    }

    // custom equals to assure content of arrays is compared
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleFact that = (SimpleFact) o;
        return Objects.deepEquals(values, that.values) && Objects.equals(predicate, that.predicate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(predicate, Arrays.hashCode(values));
    }
}
