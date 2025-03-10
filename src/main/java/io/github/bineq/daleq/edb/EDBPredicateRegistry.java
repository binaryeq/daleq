package io.github.bineq.daleq.edb;

import io.github.bineq.daleq.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility to keep track of all predicates used in the EDB.
 * @author jens dietrich
 */
public class EDBPredicateRegistry {

    public static final Map<Integer, EBDInstructionPredicate> INSTRUCTION_PREDICATES = new HashMap<>();
    public static final List<Predicate> ALL = new ArrayList<>();

    public static final Logger LOG = LoggerFactory.getLogger(EDBPredicateRegistry.class);


    static {
        LOG.info("Loading instruction predicate registry");
        URL folder = EBDInstructionPredicate.class.getResource("/instruction-predicates");
        assert folder != null;
        File dir = new File(folder.getPath());
        File[] files = dir.listFiles(f -> f.getName().endsWith(".json"));
        LOG.info("{} instruction predicates found", files.length);
        for (File file : files) {
            try {
                EBDInstructionPredicate predicate = EBDInstructionPredicate.fromJson(file);
                int opCode = predicate.getOpCode();
                if (INSTRUCTION_PREDICATES.containsKey(opCode)) {
                    LOG.warn("Duplicate instruction predicate for op code {}", opCode);
                }
                INSTRUCTION_PREDICATES.put(opCode, predicate);
            }
            catch (Exception x) {
                LOG.error("Failed to load instruction predicate from " + file.getAbsolutePath(), x);
            }
        }
        LOG.info(""+ INSTRUCTION_PREDICATES.size() + " instruction predicates loaded");


        for (EBDAdditionalPredicates additionalPredicate : EBDAdditionalPredicates.values()) {
            ALL.add(additionalPredicate);
        }
        ALL.addAll(INSTRUCTION_PREDICATES.values().stream().sorted(Comparator.comparingInt(EBDInstructionPredicate::getOpCode)).collect(Collectors.toList()));
        LOG.info(""+ ALL.size() + " predicates found");

    }

}
