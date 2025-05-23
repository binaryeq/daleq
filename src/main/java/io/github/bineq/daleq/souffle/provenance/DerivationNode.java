package io.github.bineq.daleq.souffle.provenance;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A node in a derivation tree.
 * @author jens dietrich
 */

public class DerivationNode  {

    private String id = null;
    private List<DerivationNode> children = new ArrayList<>();
    private String name = null;

    public DerivationNode(String id) {
        this.id = id;
    }

    public DerivationNode(String id, List<DerivationNode> children) {
        this.id = id;
        this.children = children;
    }

    public String getId() {
        return id;
    }

    public List<DerivationNode> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DerivationNode that = (DerivationNode) o;
        return Objects.equals(id, that.id) && Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, children);
    }
}
