package io.github.bineq.daleq.factextraction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility to keep track of all predicates
 * @author jens dietrich
 */
public class PredicateRegistry {

    public static final Map<Integer,InstructionPredicate> INSTRUCTION_PREDICATES = new HashMap<>();
    public static final List<Predicate> ALL_PREDICATES = new ArrayList<>();

    public static final Logger LOG = LoggerFactory.getLogger(PredicateRegistry.class);


    static {
        LOG.info("Loading instruction predicate registry");
        URL folder = InstructionPredicate.class.getResource("/instruction-predicates");
        assert folder != null;
        File dir = new File(folder.getPath());
        File[] files = dir.listFiles(f -> f.getName().endsWith(".json"));
        LOG.info("{} instruction predicates found", files.length);
        for (File file : files) {
            try {
                InstructionPredicate predicate = InstructionPredicate.fromJson(file);
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


        for (AdditionalPredicates additionalPredicate : AdditionalPredicates.values()) {
            ALL_PREDICATES.add(additionalPredicate);
        }
        ALL_PREDICATES.addAll(INSTRUCTION_PREDICATES.values().stream().sorted(Comparator.comparingInt(InstructionPredicate::getOpCode)).collect(Collectors.toList()));
        LOG.info(""+ ALL_PREDICATES.size() + " predicates found");

    }

}
