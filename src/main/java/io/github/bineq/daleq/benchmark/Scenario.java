package io.github.bineq.daleq.benchmark;

/**
 * A scenario consisting of two (different) bytecodes, that could be considered equivalent.
 * @param name
 * @param description
 * @param class1
 * @param class2
 * @param category
 */
public record Scenario(String name, String description, String class1, String class2,Category category) {}
