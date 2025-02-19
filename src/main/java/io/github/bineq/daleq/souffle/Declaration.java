package io.github.bineq.daleq.souffle;

import java.util.List;

/**
 * A souffle declaration.
 * @author jens dietrich
 */
public record Declaration(String relation, List<Attribute> attributes) implements SouffleElement {
}
