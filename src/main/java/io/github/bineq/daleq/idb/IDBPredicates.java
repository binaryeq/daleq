package io.github.bineq.daleq.idb;

import com.google.common.base.Preconditions;
import io.github.bineq.daleq.edb.Predicate;

import java.util.Arrays;

/**
 * Conversion of EDB predicates into IDB predicates.
 * @author jens dietrich
 */
public class IDBPredicates {

    public static final String PREFIX = "IDB_";

    public String getIDBPredicateName(Predicate predicate) {
        Preconditions.checkArgument(!isIDBPredicate(predicate));
        return getIDBPredicateName(predicate.getName());
    }

    private String getIDBPredicateName(String name) {
        return PREFIX + name;
    }

    public boolean isIDBPredicateName(String name) {
        return name.startsWith(PREFIX) || AdditionalPredicates.valuesAsSet().stream().map(AdditionalPredicates::name).anyMatch(name::equals);
    }

    public boolean isIDBPredicate(Predicate predicate) {
        return AdditionalPredicates.valuesAsSet().contains(predicate) || isIDBPredicateName(predicate.getName());
    }
}
