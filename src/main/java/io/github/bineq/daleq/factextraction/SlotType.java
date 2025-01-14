package io.github.bineq.daleq.factextraction;

/**
 * Souffle type.
 * @author jens dietrich
 */
public enum SlotType {

    SYMBOL, NUMBER, UNSIGNED, FLOAT;

    public String souffleType() {
        return name().toLowerCase();
    }
}
