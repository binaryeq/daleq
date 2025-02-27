package io.github.bineq.daleq.edb;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates ids to be used in facts.
 * Ids are unique only within the JVM instance / classloader.
 * @author jens dietrich
 */
public class FactIdGenerator {
    public static final AtomicInteger COUNTER = new AtomicInteger(0);

    public static String nextId(Predicate predicate) {
        return "F"+COUNTER.incrementAndGet();
    }
}
