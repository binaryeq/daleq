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
    SUPERCLASS(symslot("factId"),symslot("name"),symslot("supername")),
    INTERFACE(symslot("factId"),symslot("name"),symslot("interface")),
    FIELD(symslot("factId"),symslot("id"),symslot("classname"),symslot("name"),symslot("descriptor")),
    METHOD(symslot("factId"),symslot("id"),symslot("classname"),symslot("name"),symslot("descriptor")),
    VERSION(symslot("factId"),symslot("classname"),symslot("version")),

    // field properties
    FIELD_SIGNATURE(symslot("factId"),symslot("fieldid"),symslot("signature")),

    // method properties
    METHOD_SIGNATURE(symslot("factId"),symslot("methodid"),symslot("signature"))
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
