package de.evilcodez.config.serialization.object.defaults;

import de.evilcodez.config.BaseValue;
import de.evilcodez.config.NumberValue;
import de.evilcodez.config.serialization.object.ObjectSerializer;
import de.evilcodez.config.serialization.object.SerializationContext;
import de.evilcodez.config.serialization.object.TypeSerializer;
import de.evilcodez.config.serialization.object.path.ValuePath;

import java.awt.*;

public class ColorSerializer implements TypeSerializer<Color> {

    @Override
    public BaseValue serialize(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, Color value, Class<?> typeClass) {
        return new NumberValue(value.getRGB());
    }

    @Override
    public Color deserialize(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, BaseValue value, Class<?> typeClass) {
        return new Color(((NumberValue) value).getValue().intValue());
    }
}
