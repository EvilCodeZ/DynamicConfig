package de.evilcodez.config.serialization.object.defaults;

import de.evilcodez.config.BaseValue;
import de.evilcodez.config.StringValue;
import de.evilcodez.config.serialization.object.ObjectSerializer;
import de.evilcodez.config.serialization.object.TypeSerializer;

import java.util.UUID;

public class UUIDSerializer implements TypeSerializer<UUID> {

    @Override
    public BaseValue serialize(ObjectSerializer serializer, UUID value, Class<?> typeClass) {
        return new StringValue(value.toString());
    }

    @Override
    public UUID deserialize(ObjectSerializer serializer, BaseValue value, Class<?> typeClass) {
        return UUID.fromString(((StringValue) value).getValue());
    }
}
