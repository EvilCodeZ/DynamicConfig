package de.evilcodez.config.serialization.object;

import de.evilcodez.config.BaseValue;

public interface TypeSerializer<T> {

    BaseValue serialize(ObjectSerializer serializer, T value, Class<?> typeClass);

    T deserialize(ObjectSerializer serializer, BaseValue value, Class<?> typeClass);
}
