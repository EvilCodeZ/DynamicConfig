package de.evilcodez.config.serialization.object.path;

public interface ValuePath {

    ValuePath getParentNode();

    static String toString(ValuePath path) {
        return path == null ? "root" : String.valueOf(path);
    }
}
