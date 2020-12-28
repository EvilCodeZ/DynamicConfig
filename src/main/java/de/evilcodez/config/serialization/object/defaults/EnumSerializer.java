package de.evilcodez.config.serialization.object.defaults;

import de.evilcodez.config.BaseValue;
import de.evilcodez.config.NullValue;
import de.evilcodez.config.StringValue;
import de.evilcodez.config.serialization.object.ObjectSerializer;
import de.evilcodez.config.serialization.object.SerializationContext;
import de.evilcodez.config.serialization.object.TypeSerializer;
import de.evilcodez.config.serialization.object.path.ValuePath;

import java.util.Arrays;
import java.util.Optional;

public class EnumSerializer implements TypeSerializer<Enum> {

    @Override
    public BaseValue serialize(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, Enum value, Class<?> typeClass) {
        return new StringValue(value.name());
    }

    @Override
    public Enum deserialize(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, BaseValue value, Class<?> typeClass) {
        if(value instanceof NullValue) {
            return null;
        }
        final Optional<Enum> opt = Arrays.stream(typeClass.getEnumConstants())
                .map(val -> (Enum) val)
                .filter(val -> val.name().equals(((StringValue) value).getValue()))
                .findFirst();
        return opt.orElse(null);
    }
}
