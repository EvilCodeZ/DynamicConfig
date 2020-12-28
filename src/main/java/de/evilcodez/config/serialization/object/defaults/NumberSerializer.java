package de.evilcodez.config.serialization.object.defaults;

import de.evilcodez.config.BaseValue;
import de.evilcodez.config.NullValue;
import de.evilcodez.config.NumberValue;
import de.evilcodez.config.serialization.object.ObjectSerializer;
import de.evilcodez.config.serialization.object.SerializationContext;
import de.evilcodez.config.serialization.object.TypeSerializer;
import de.evilcodez.config.serialization.object.path.ValuePath;

public class NumberSerializer implements TypeSerializer<Number> {

    @Override
    public BaseValue serialize(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, Number value, Class<?> typeClass) {
        return new NumberValue(value);
    }

    @Override
    public Number deserialize(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, BaseValue value, Class<?> typeClass) {
        if(value instanceof NullValue) {
            return null;
        }
        return ((NumberValue) value).getValue();
    }
}
