package io.github.bineq.daleq.factextraction;

/**
 * An eleemt of a record, souffle call them fields, slot is used here to avoid confusion with Java fields.
 * @author jens dietrich
 */
public record Slot(String name,SlotType type) {

    static Slot symslot(String name) {
        return new Slot(name, SlotType.symbol);
    }

    static Slot numslot(String name) {
        return new Slot(name, SlotType.number);
    }
}
