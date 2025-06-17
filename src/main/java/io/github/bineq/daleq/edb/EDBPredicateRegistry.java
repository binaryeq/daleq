package io.github.bineq.daleq.edb;

import io.github.bineq.daleq.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath*:instruction-predicates/*.json");
            for (Resource resource : resources) {
                String json = resource.getContentAsString(StandardCharsets.UTF_8);
                EBDInstructionPredicate predicate = EBDInstructionPredicate.fromJson(json);
                int opCode = predicate.getOpCode();
                if (INSTRUCTION_PREDICATES.containsKey(opCode)) {
                    LOG.warn("Duplicate instruction predicate for op code {}", opCode);
                }
                INSTRUCTION_PREDICATES.put(opCode, predicate);
            }
        } catch (IOException e) {
            LOG.error("Failed to load instruction predicate registry", e);
            System.exit(2);
        }

        LOG.info(""+ INSTRUCTION_PREDICATES.size() + " instruction predicates loaded");


        for (EBDAdditionalPredicates additionalPredicate : EBDAdditionalPredicates.values()) {
            ALL.add(additionalPredicate);
        }
        ALL.addAll(INSTRUCTION_PREDICATES.values().stream().sorted(Comparator.comparingInt(EBDInstructionPredicate::getOpCode)).collect(Collectors.toList()));
        LOG.info(""+ ALL.size() + " predicates found");

    }

}
