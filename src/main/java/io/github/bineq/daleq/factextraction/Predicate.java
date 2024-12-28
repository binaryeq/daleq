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
    INSTRUCTION(symslot("methodId"),numslot("instructioncounter"),symslot("instruction")),
    FIELD_INS(symslot("methodId"),numslot("instructioncounter"),symslot("owner"),symslot("name"),symslot("descriptor"),symslot("instruction")),
    METHOD_INS(symslot("methodId"),numslot("instructioncounter"),symslot("owner"),symslot("name"),symslot("descriptor"),symslot("instruction")),
    TYPE_INSN(symslot("methodId"),numslot("instructioncounter"),symslot("descriptor")),
    DUP(symslot("methodId"),numslot("instructioncounter")),
    ATHROW(symslot("methodId"),numslot("instructioncounter")),
    JSR(symslot("methodId"),numslot("instructioncounter")),

    ARETURN(symslot("methodId"),numslot("instructioncounter")),
    DRETURN(symslot("methodId"),numslot("instructioncounter")),
    FRETURN(symslot("methodId"),numslot("instructioncounter")),
    IRETURN(symslot("methodId"),numslot("instructioncounter")),
    LRETURN(symslot("methodId"),numslot("instructioncounter")),
    RETURN(symslot("methodId"),numslot("instructioncounter")),

    LDC(symslot("methodId"),numslot("instructioncounter"),symslot("value")),
    VAR_INST(symslot("methodId"),numslot("instructioncounter"),numslot("variable")),
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
