package io.github.bineq.daleq.edb;


import static io.github.bineq.daleq.edb.Slot.*;

/**
 * Predicates used in the extract DB.
 * @author jens dietrich
 */
public enum AdditionalPredicates implements Predicate {



    // class properties
    SUPERCLASS(symslot(Fact.ID_SLOT_NAME),symslot("name"),symslot("supername")),
    INTERFACE(symslot(Fact.ID_SLOT_NAME),symslot("name"),symslot("interface")),
    FIELD(symslot(Fact.ID_SLOT_NAME),symslot("id"),symslot("classname"),symslot("name"),symslot("descriptor")),
    METHOD(symslot(Fact.ID_SLOT_NAME),symslot("id"),symslot("classname"),symslot("name"),symslot("descriptor")),
    VERSION(symslot(Fact.ID_SLOT_NAME),symslot("classname"),symslot("version")),

    // field properties
    FIELD_SIGNATURE(symslot(Fact.ID_SLOT_NAME),symslot("fieldid"),symslot("signature")),

    // method properties
    METHOD_SIGNATURE(symslot(Fact.ID_SLOT_NAME),symslot("methodid"),symslot("signature"))
    ;

    public final Slot[] slots;

    AdditionalPredicates(Slot... slots) {
        this.slots = slots;
    }

    @Override
    public Slot[] getSlots() {
        return slots;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public boolean isInstructionPredicate() {
        return false;
    }


}
