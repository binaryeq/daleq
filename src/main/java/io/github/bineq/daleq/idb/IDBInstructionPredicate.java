package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Predicate;
import io.github.bineq.daleq.Slot;
import java.util.Arrays;
import java.util.Objects;

/**
 * Predicates used in the IDB.
 * @author jens dietrich
 */
public class IDBInstructionPredicate implements Predicate {

    private Slot[] slots = null;
    private String name = null;
    private boolean  isInstructionPredicate = false;

    public IDBInstructionPredicate(String name, Slot[] slots,boolean isInstructionPredicate) {
        this.slots = slots;
        this.name = name;
        this.isInstructionPredicate = isInstructionPredicate;
    }

    @Override
    public Slot[] getSlots() {
        return slots;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isInstructionPredicate() {
        return this.isInstructionPredicate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IDBInstructionPredicate that = (IDBInstructionPredicate) o;
        return Objects.deepEquals(slots, that.slots) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(slots), name);
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
