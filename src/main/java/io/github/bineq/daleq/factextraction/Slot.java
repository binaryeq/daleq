package io.github.bineq.daleq.factextraction;

/**
 * An element of a record, souffle calls them fields, slot is used here to avoid confusion with Java fields.
 * @author jens dietrich
 */
public record Slot(String name,SlotType type,String jtype) {

    static Slot symslot(String name) {
        return new Slot(name, SlotType.SYMBOL,String.class.getName());
    }

    static Slot symslot(String name,String jname) {
        return new Slot(name, SlotType.SYMBOL,jname);
    }

    static Slot numslot(String name,String jname) {
        return new Slot(name, SlotType.NUMBER,jname);
    }
}
