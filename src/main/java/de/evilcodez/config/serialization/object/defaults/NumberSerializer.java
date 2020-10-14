package de.evilcodez.config.serialization.object.defaults;

import de.evilcodez.config.BaseValue;
import de.evilcodez.config.NullValue;
import de.evilcodez.config.NumberValue;
import de.evilcodez.config.serialization.object.ObjectSerializer;
import de.evilcodez.config.serialization.object.TypeSerializer;

public class NumberSerializer implements TypeSerializer<Number> {

    @Override
    public BaseValue serialize(ObjectSerializer serializer, Number value, Class<?> typeClass) {
        return new NumberValue(value);
    }

    @Override
    public Number deserialize(ObjectSerializer serializer, BaseValue value, Class<?> typeClass) {
        if(value instanceof NullValue) {
            return null;
        }
        return ((NumberValue) value).getValue();
    }
}
