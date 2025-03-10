package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Predicate;
import io.github.bineq.daleq.edb.EDBPredicateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * Utility to keep track of all predicates used in the IDB.
 * @author jens dietrich
 */
public class IDBPredicateRegistry {

    // organised by name
    public static final Map<String,Predicate> ALL = new HashMap<>();

    public static final Logger LOG = LoggerFactory.getLogger(IDBPredicateRegistry.class);

    static {
        LOG.info("Loading and converting EDB predicates");
        EDBPredicateRegistry.ALL.stream()
            .map(edbPredicate -> new IDBInstructionPredicate(IDBPredicates.convertPredicateNameToIDB(edbPredicate.getName()),edbPredicate.getSlots().clone(),edbPredicate.isInstructionPredicate()))
                .forEach(idbPredicate -> {
                    Predicate previous = ALL.put(idbPredicate.getName(),idbPredicate);
                    assert previous == null : "two predicates with the same name exists";
                });
        LOG.info(""+ ALL.size() + " instruction predicates loaded");

        // adding access predicates
        for (Predicate predicate : IDBAccessPredicates.ALL) {
            ALL.put(predicate.getName(),predicate);
        }
        LOG.info(""+ IDBAccessPredicates.ALL.size() + " access predicates added");

        for (IDBAdditionalPredicates additionalPredicate : IDBAdditionalPredicates.values()) {
            Predicate previous = ALL.put(additionalPredicate.getName(),additionalPredicate);
            assert previous == null : "two predicates with the same name exists";
        }

        LOG.info(""+ ALL.size() + " predicates found");

    }

}
