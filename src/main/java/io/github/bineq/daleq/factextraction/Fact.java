package io.github.bineq.daleq.factextraction;

public interface Fact {
    AdditionalPredicates predicate();

    Object[] values();

    String asSouffleFact();

    // check fact for consistency with schema
    default void verify() throws VerificationException {
        AdditionalPredicates predicate = predicate();
        if (predicate.slots.length != values().length) {
            throw new VerificationException("Unexpected NUMBER of values in fact for predicate " + predicate.name() + ", is " + values().length + ", expected " + predicate.slots.length);
        }

        assert predicate.slots.length == values().length;

        for (int i = 0; i < values().length; i++) {
            Slot slot = predicate.slots[i];
            Object obj = values()[i];

            if (slot.type()==SlotType.NUMBER && !(obj instanceof Number)) {
                throw new VerificationException("Unexpected value type in value at index " + i + ", is : " + obj.getClass().getName() + ", expected subtypeof " + Number.class.getName());
            }
        }


    }
}
