package de.evilcodez.config.serialization.object.path;

import java.util.Objects;

public class ListValuePath implements ValuePath {

    private int index;
    private ValuePath parentNode;

    public ListValuePath(int index, ValuePath parentNode) {
        this.index = index;
        this.parentNode = parentNode;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public ValuePath getParentNode() {
        return parentNode;
    }

    public void setParentNode(ValuePath parentNode) {
        this.parentNode = parentNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListValuePath that = (ListValuePath) o;
        return index == that.index && Objects.equals(parentNode, that.parentNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, parentNode);
    }

    @Override
    public String toString() {
        return ValuePath.toString(parentNode) + "[" + index + "]";
    }
}
