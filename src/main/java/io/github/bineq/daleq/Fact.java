package io.github.bineq.daleq;

import io.github.bineq.daleq.edb.VerificationException;

public interface Fact {

    String ID_SLOT_NAME = "factid";
    Predicate predicate();

    Object[] values();

    String asSouffleFact();

    // check fact for consistency with schema
    default void verify() throws VerificationException {
        Predicate predicate = predicate();
        if (predicate.getSlots().length != values().length) {
            throw new VerificationException("Unexpected NUMBER of values in fact for predicate " + predicate.getName() + ", is " + values().length + ", expected " + predicate.getSlots().length);
        }

        assert predicate.getSlots().length == values().length;

        for (int i = 0; i < values().length; i++) {
            Slot slot = predicate.getSlots()[i];
            Object obj = values()[i];

            if (slot.type()==SlotType.NUMBER && !((obj instanceof Number) || (obj instanceof Boolean))) {
                throw new VerificationException("Unexpected value type in value at index " + i + ", is : " + obj.getClass().getName() + ", expected subtypeof " + Number.class.getName());
            }
        }
    }
}
