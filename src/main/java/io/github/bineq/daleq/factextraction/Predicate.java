package io.github.bineq.daleq.factextraction;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Predicates used in the extract DB.
 * @author jens dietrich
 */
public interface Predicate {

    // file name without facts extension
    default String asSouffleFactFileName() {
        return getName();
    }

    default String asSouffleFactImportStatement() {
        return ".input " + asSouffleFactFileName() + " // facts are imported from " + asSouffleFactFileNameWithExtension();
    }

    // file name with facts extension
    default String asSouffleFactFileNameWithExtension() {
        return asSouffleFactFileName() + ".facts";
    }

    default String asSouffleDecl() {
        String pre =  ".decl " + this.getName() + '(';
        String post =  ")";
        return Arrays.stream(getSlots()).map(slot -> slot.name() + ": " + slot.type().name()).collect(Collectors.joining(",",pre, post));
    }

    Slot[] getSlots();

    String getName();

}
