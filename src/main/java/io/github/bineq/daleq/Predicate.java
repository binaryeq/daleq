package io.github.bineq.daleq;

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
        return Arrays.stream(getSlots()).map(
            slot -> slot.encodeName() + ": " + slot.type().souffleType()).collect(Collectors.joining(",",pre, post));
    }

    Slot[] getSlots();

    String getName();

    boolean isInstructionPredicate();

    boolean isEDBPredicate();

    boolean isIDBPredicate();


    /**
     * Utility to parse facts, i.e. importing TSV data.
     * Sometimes data is missing if the last value is an empty string.
     * This method "fills" parsed values with emty strings.
     * @param tokens
     * @return
     */
    static String[] pad(Predicate predicate, String[] tokens) {

        if (tokens.length == predicate.getSlots().length - 1) {
            String[] tokens2 = new String[predicate.getSlots().length];
            for (int i = 0; i < tokens.length; i++) {
                tokens2[i] = tokens[i];
            }
            tokens2[tokens2.length - 1] = "";
            return tokens2;
        }
        else if (tokens.length == predicate.getSlots().length) {
            return tokens;
        }
        assert false;
        return tokens;

    }

}
