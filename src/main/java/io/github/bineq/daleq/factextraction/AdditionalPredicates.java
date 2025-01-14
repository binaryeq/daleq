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
    SUPERCLASS(symslot("name"),symslot("supername")),
    INTERFACE(symslot("name"),symslot("interface")),
    FIELD(symslot("id"),symslot("classname"),symslot("name"),symslot("descriptor")),
    METHOD(symslot("id"),symslot("classname"),symslot("name"),symslot("descriptor")),
    VERSION(symslot("classname"),symslot("version")),

    // field properties
    FIELD_SIGNATURE(symslot("fieldid"),symslot("signature")),

    // method properties
    METHOD_SIGNATURE(symslot("methodid"),symslot("signature"))
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
