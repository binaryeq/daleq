package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Fact;
import io.github.bineq.daleq.Predicate;
import io.github.bineq.daleq.Slot;
import java.util.Set;
import static io.github.bineq.daleq.Slot.numslot;
import static io.github.bineq.daleq.Slot.symslot;

/**
 * Predicates related to the removal of facts (i.e. facts not being copied from the EDB to the IDB) or the moving of instructions
 * (into other methods).
 * Present in the IDB to provide provenance.
 * @author jens dietrich
 */
public enum IDBRemovalPredicates implements Predicate {

    REMOVED_INSTRUCTION(symslot(Fact.ID_SLOT_NAME),symslot("methodid"),numslot("instructioncounter",Integer.TYPE.getName())) {
        @Override
        public boolean isInstructionPredicate() {
            return true;
        }
    },
    MOVED_INSTRUCTION(
        symslot(Fact.ID_SLOT_NAME),
        symslot("methodid1"), numslot("instructioncounter1",Integer.TYPE.getName()),
        symslot("methodid2"), numslot("instructioncounter2",Integer.TYPE.getName()),
        symslot("instructionname")
    ) {
        @Override
        public boolean isInstructionPredicate() {
            return true;
        }
    },
    REMOVED_METHOD(symslot(Fact.ID_SLOT_NAME),symslot("id")),
    REMOVED_FIELD(symslot(Fact.ID_SLOT_NAME),symslot("id")),
    REMOVED_VERSION(symslot(Fact.ID_SLOT_NAME),symslot("id")),
    REMOVED_ANNOTATION(symslot(Fact.ID_SLOT_NAME),symslot("classOrMethodOrFieldId"),symslot("annotation")),
    REMOVED_ACCESS(symslot(Fact.ID_SLOT_NAME),symslot("classOrMethodOrFieldId")),
    REMOVED_SIGNATURE(symslot(Fact.ID_SLOT_NAME),symslot("classOrMethodOrFieldId"))
    ;

    public static Set<IDBRemovalPredicates> valuesAsSet() {
        return Set.of(values());
    };

    public final Slot[] slots;

    IDBRemovalPredicates(Slot... slots) {
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
        return false;
    }

    @Override
    public boolean isIDBPredicate() {
        return true;
    }


}
