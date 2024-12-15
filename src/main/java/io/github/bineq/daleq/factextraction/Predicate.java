package io.github.bineq.daleq.factextraction;


import java.util.Arrays;
import java.util.stream.Collectors;

import static io.github.bineq.daleq.factextraction.Slot.*;

/**
 * Predicates used in the extract DB.
 * @author jens dietrich
 */
public enum Predicate {



    // class properties
    SUPERCLASS(symslot("name"),symslot("supername")),
    INTERFACE(symslot("name"),symslot("interface")),
    FIELD(symslot("id"),symslot("classname"),symslot("name"),symslot("descriptor")),
    METHOD(symslot("id"),symslot("classname"),symslot("name"),symslot("descriptor")),
    VERSION(symslot("classname"),symslot("version")),

    // field properties
    FIELD_SIGNATURE(symslot("fieldid"),symslot("signature")),

    // method properties
    METHOD_SIGNATURE(symslot("methodid"),symslot("signature")),

    // instructions
    INSTRUCTION(symslot("methodId"),symslot("instruction")),
    FIELD_INS(symslot("methodId"),symslot("name"),symslot("descriptor"),symslot("instruction")),
    ;

    public final Slot[] slots;

    Predicate(Slot... slots) {
        this.slots = slots;
    }

    String asSouffleDecl() {
        String pre =  ".decl " + this.name() + '(';
        String post =  ")";
        return Arrays.stream(slots).map(slot -> slot.name() + ": " + slot.type().name()).collect(Collectors.joining(",",pre, post));
    }

    // file name without facts extension
    String asSouffleFactFileName() {
        return name();
    }

    String asSouffleFactImportStatement() {
        return ".input " + asSouffleFactFileName() + " // facts are imported from " + asSouffleFactFileNameWithExtension();
    }

    // file name with facts extension
    String asSouffleFactFileNameWithExtension() {
        return asSouffleFactFileName() + ".facts";
    }

}
