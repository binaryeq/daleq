package io.github.bineq.daleq.souffle.provenance;

import java.util.ArrayList;
import java.util.List;

/**
 * A node in a derivation tree.
 * @param id
 * @param children
 * @author jens dietrich
 */

public record DerivationNode(String id, List<DerivationNode> children)  {

    public DerivationNode(String id) {
        this(id, new ArrayList<>());
    }
}
