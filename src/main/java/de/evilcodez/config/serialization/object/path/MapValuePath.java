package de.evilcodez.config.serialization.object.path;

import de.evilcodez.config.utils.ConfigUtils;

import java.util.Objects;

public class MapValuePath implements ValuePath {

    private String key;
    private ValuePath parentNode;

    public MapValuePath(String key, ValuePath parentNode) {
        this.key = key;
        this.parentNode = parentNode;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
        MapValuePath that = (MapValuePath) o;
        return Objects.equals(key, that.key) && Objects.equals(parentNode, that.parentNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, parentNode);
    }

    @Override
    public String toString() {
        return ValuePath.toString(parentNode) + "->\"" + ConfigUtils.escapeString(key) + "\"";
    }
}
