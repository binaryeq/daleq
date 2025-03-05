package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.edb.Fact;
import io.github.bineq.daleq.edb.Predicate;
import io.github.bineq.daleq.edb.Slot;
import java.util.Set;
import static io.github.bineq.daleq.edb.Slot.numslot;
import static io.github.bineq.daleq.edb.Slot.symslot;

/**
 * Predicates used only in the IDB.
 * @author jens dietrich
 */
public enum AdditionalPredicates implements Predicate {


    // placeholder if a rule remove a fact (e.g. a redundant checkcast)
    // a NOPE fact will still carry provenance that references the removed fact
    // and the context (method + instruction id)
    NOPE(symslot(Fact.ID_SLOT_NAME),symslot("methodid"),numslot("instructioncounter",Integer.TYPE.getName()));

    public static Set<AdditionalPredicates> valuesAsSet() {
        return Set.of(values());
    };

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
