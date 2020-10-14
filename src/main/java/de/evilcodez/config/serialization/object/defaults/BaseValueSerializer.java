package de.evilcodez.config.serialization.object.defaults;

import de.evilcodez.config.BaseValue;
import de.evilcodez.config.serialization.object.ObjectSerializer;
import de.evilcodez.config.serialization.object.TypeSerializer;

public class BaseValueSerializer implements TypeSerializer<BaseValue> {

    @Override
    public BaseValue serialize(ObjectSerializer serializer, BaseValue value, Class<?> typeClass) {
        return value;
    }

    @Override
    public BaseValue deserialize(ObjectSerializer serializer, BaseValue value, Class<?> typeClass) {
        return value;
    }
}
