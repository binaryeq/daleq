package io.github.bineq.daleq.idb;

import com.google.common.base.Preconditions;
import io.github.bineq.daleq.Predicate;

/**
 * Conversion of EDB predicates into IDB predicates.
 * @author jens dietrich
 */
public class IDBPredicates {

    private static final String PREFIX = "IDB_";

    static String convertPredicateNameToIDB(String name) {
        Preconditions.checkArgument(!isIDBPredicateName(name));
        return PREFIX + name;
    }

    static boolean isIDBPredicateName(String name) {
        return name.startsWith(PREFIX) || IDBAdditionalPredicates.valuesAsSet().stream().map(IDBAdditionalPredicates::name).anyMatch(name::equals);
    }

}
