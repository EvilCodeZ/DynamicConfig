package de.evilcodez.config.serialization.object.defaults;

import de.evilcodez.config.BaseValue;
import de.evilcodez.config.BooleanValue;
import de.evilcodez.config.NullValue;
import de.evilcodez.config.serialization.object.ObjectSerializer;
import de.evilcodez.config.serialization.object.TypeSerializer;

public class BooleanSerializer implements TypeSerializer<Boolean> {

    @Override
    public BaseValue serialize(ObjectSerializer serializer, Boolean value, Class<?> typeClass) {
        return new BooleanValue(value);
    }

    @Override
    public Boolean deserialize(ObjectSerializer serializer, BaseValue value, Class<?> typeClass) {
        if(value instanceof NullValue) {
            return null;
        }
        return ((BooleanValue) value).getValue();
    }
}
