package de.evilcodez.config.serialization.object.defaults;

import de.evilcodez.config.BaseValue;
import de.evilcodez.config.CharValue;
import de.evilcodez.config.NullValue;
import de.evilcodez.config.serialization.object.ObjectSerializer;
import de.evilcodez.config.serialization.object.SerializationContext;
import de.evilcodez.config.serialization.object.TypeSerializer;
import de.evilcodez.config.serialization.object.path.ValuePath;

public class CharacterSerializer implements TypeSerializer<Character> {

    @Override
    public BaseValue serialize(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, Character value, Class<?> typeClass) {
        return new CharValue(value);
    }

    @Override
    public Character deserialize(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, BaseValue value, Class<?> typeClass) {
        if(value instanceof NullValue) {
            return null;
        }
        return ((CharValue) value).getValue();
    }
}
