package de.evilcodez.config.serialization.object.defaults;

import de.evilcodez.config.BaseValue;
import de.evilcodez.config.CharValue;
import de.evilcodez.config.NullValue;
import de.evilcodez.config.serialization.object.ObjectSerializer;
import de.evilcodez.config.serialization.object.TypeSerializer;

public class CharacterSerializer implements TypeSerializer<Character> {

    @Override
    public BaseValue serialize(ObjectSerializer serializer, Character value, Class<?> typeClass) {
        return new CharValue(value);
    }

    @Override
    public Character deserialize(ObjectSerializer serializer, BaseValue value, Class<?> typeClass) {
        if(value instanceof NullValue) {
            return null;
        }
        return ((CharValue) value).getValue();
    }
}
