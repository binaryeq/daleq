package io.github.bineq.daleq.idb;

import io.github.bineq.daleq.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

import static io.github.bineq.daleq.edb.EDBPredicateRegistry.INSTRUCTION_PREDICATES;

/**
 * Utility to keep track of all predicates used in the IDB.
 * @author jens dietrich
 */
public class IDBPredicateRegistry {

    // organised by name
    public static final Map<String,Predicate> ALL_PREDICATES = new HashMap<>();

    public static final Logger LOG = LoggerFactory.getLogger(IDBPredicateRegistry.class);

    static {
        LOG.info("Loading instruction predicate registry");
        INSTRUCTION_PREDICATES.values().stream()
            .map(edbPredicate -> new IDBInstructionPredicate(IDBPredicates.convertPredicateNameToIDB(edbPredicate.getName()),edbPredicate.getSlots().clone()))
                .forEach(idbPredicate -> {
                    Predicate previous = ALL_PREDICATES.put(idbPredicate.getName(),idbPredicate);
                    assert previous == null : "two predicates with the same name exists";
                });


        LOG.info(""+ ALL_PREDICATES.size() + " instruction predicates loaded");

        for (IDBAdditionalPredicates additionalPredicate : IDBAdditionalPredicates.values()) {
            Predicate previous = ALL_PREDICATES.put(additionalPredicate.getName(),additionalPredicate);
            assert previous == null : "two predicates with the same name exists";
        }

        LOG.info(""+ ALL_PREDICATES.size() + " predicates found");

    }

}
