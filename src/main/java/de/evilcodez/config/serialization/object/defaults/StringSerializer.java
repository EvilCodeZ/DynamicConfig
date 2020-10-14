package de.evilcodez.config.serialization.object.defaults;

import de.evilcodez.config.BaseValue;
import de.evilcodez.config.NullValue;
import de.evilcodez.config.StringValue;
import de.evilcodez.config.serialization.object.ObjectSerializer;
import de.evilcodez.config.serialization.object.TypeSerializer;

public class StringSerializer implements TypeSerializer<String> {

    @Override
    public BaseValue serialize(ObjectSerializer serializer, String value, Class<?> typeClass) {
        return new StringValue(value);
    }

    @Override
    public String deserialize(ObjectSerializer serializer, BaseValue value, Class<?> typeClass) {
        if(value instanceof NullValue) {
            return null;
        }
        return ((StringValue) value).getValue();
    }
}
