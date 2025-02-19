package io.github.bineq.daleq.factextraction;


import java.util.Arrays;
import java.util.stream.Collectors;
import static io.github.bineq.daleq.factextraction.Slot.*;

/**
 * Predicates used in the extract DB.
 * @author jens dietrich
 */
public enum AdditionalPredicates implements Predicate {



    // class properties
<<<<<<< HEAD
    SUPERCLASS(symslot(Fact.ID_SLOT_NAME),symslot("name"),symslot("supername")),
    INTERFACE(symslot(Fact.ID_SLOT_NAME),symslot("name"),symslot("interface")),
    FIELD(symslot(Fact.ID_SLOT_NAME),symslot("id"),symslot("classname"),symslot("name"),symslot("descriptor")),
    METHOD(symslot(Fact.ID_SLOT_NAME),symslot("id"),symslot("classname"),symslot("name"),symslot("descriptor")),
    VERSION(symslot(Fact.ID_SLOT_NAME),symslot("classname"),symslot("version")),

    // field properties
    FIELD_SIGNATURE(symslot(Fact.ID_SLOT_NAME),symslot("fieldid"),symslot("signature")),

    // method properties
    METHOD_SIGNATURE(symslot(Fact.ID_SLOT_NAME),symslot("methodid"),symslot("signature"))
=======
    SUPERCLASS(symslot("factId"),symslot("name"),symslot("supername")),
    INTERFACE(symslot("factId"),symslot("name"),symslot("interface")),
    FIELD(symslot("factId"),symslot("id"),symslot("classname"),symslot("name"),symslot("descriptor")),
    METHOD(symslot("factId"),symslot("id"),symslot("classname"),symslot("name"),symslot("descriptor")),
    VERSION(symslot("factId"),symslot("classname"),symslot("version")),

    // field properties
    FIELD_SIGNATURE(symslot("factId"),symslot("fieldid"),symslot("signature")),

    // method properties
    METHOD_SIGNATURE(symslot("factId"),symslot("methodid"),symslot("signature"))
>>>>>>> 53b61a67ecfe151d2d821f2a207ffaee6b9d2d98
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
