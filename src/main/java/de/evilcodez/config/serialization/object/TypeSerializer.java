package de.evilcodez.config.serialization.object;

import de.evilcodez.config.BaseValue;
import de.evilcodez.config.serialization.object.path.ValuePath;

public interface TypeSerializer<T> {

    BaseValue serialize(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, T value, Class<?> typeClass);

    T deserialize(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, BaseValue value, Class<?> typeClass);

    default void postDeserialize(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, T value) {}
}
