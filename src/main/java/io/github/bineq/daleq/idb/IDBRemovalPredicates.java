package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Fact;
import io.github.bineq.daleq.Predicate;
import io.github.bineq.daleq.Slot;
import java.util.Set;
import static io.github.bineq.daleq.Slot.numslot;
import static io.github.bineq.daleq.Slot.symslot;

/**
 * Predicates related to the removal of facts (i.e. facts not being copied from the EDB to the IDB).
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
    REMOVED_METHOD(symslot(Fact.ID_SLOT_NAME),symslot("id")),
    REMOVED_FIELD(symslot(Fact.ID_SLOT_NAME),symslot("id"))
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
