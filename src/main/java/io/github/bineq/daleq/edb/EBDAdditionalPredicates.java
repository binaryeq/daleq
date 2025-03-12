package io.github.bineq.daleq.edb;


import io.github.bineq.daleq.Fact;
import io.github.bineq.daleq.Predicate;
import io.github.bineq.daleq.Slot;
import org.objectweb.asm.tree.LabelNode;

import java.util.Set;

import static io.github.bineq.daleq.Slot.*;

/**
 * Predicates used in the extract DB.
 * @author jens dietrich
 */
public enum EBDAdditionalPredicates implements Predicate {

    // class properties
    SUPERCLASS(symslot(Fact.ID_SLOT_NAME),symslot("name"),symslot("supername")),
    INTERFACE(symslot(Fact.ID_SLOT_NAME),symslot("name"),symslot("interface")),
    FIELD(symslot(Fact.ID_SLOT_NAME),symslot("id"),symslot("classname"),symslot("name"),symslot("descriptor")),
    METHOD(symslot(Fact.ID_SLOT_NAME),symslot("id"),symslot("classname"),symslot("name"),symslot("descriptor")),
    VERSION(symslot(Fact.ID_SLOT_NAME),symslot("classname"),numslot("version",Integer.TYPE.getName())),
    CLASS_SIGNATURE(symslot(Fact.ID_SLOT_NAME),symslot("classname"),symslot("signature")),

    // field properties
    FIELD_SIGNATURE(symslot(Fact.ID_SLOT_NAME),symslot("fieldid"),symslot("signature")),

    // method properties
    METHOD_SIGNATURE(symslot(Fact.ID_SLOT_NAME),symslot("methodid"),symslot("signature")),

    // property of classes, interfaces and methods
    // the id is the unique name of this class, method or field
    ACCESS(symslot(Fact.ID_SLOT_NAME),symslot("id"),numslot("access",Integer.TYPE.getName())),

    // labels are represented by instruction-type facts
    LABEL(symslot(Fact.ID_SLOT_NAME),symslot("methodid"),numslot("instructioncounter",Integer.TYPE.getName()),symslot("labelid")) {
        @Override
        public boolean isInstructionPredicate() {
            return true;
        }
    },
    ;


    public static Set<EBDAdditionalPredicates> valuesAsSet() {
        return Set.of(values());
    };

    public final Slot[] slots;

    EBDAdditionalPredicates(Slot... slots) {
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

    @Override
    public boolean isEDBPredicate() {
        return true;
    }

    @Override
    public boolean isIDBPredicate() {
        return false;
    }

}
