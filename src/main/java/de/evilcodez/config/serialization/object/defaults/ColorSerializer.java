package de.evilcodez.config.serialization.object.defaults;

import de.evilcodez.config.BaseValue;
import de.evilcodez.config.NumberValue;
import de.evilcodez.config.serialization.object.ObjectSerializer;
import de.evilcodez.config.serialization.object.TypeSerializer;

import java.awt.*;

public class ColorSerializer implements TypeSerializer<Color> {

    @Override
    public BaseValue serialize(ObjectSerializer serializer, Color value, Class<?> typeClass) {
        return new NumberValue(value.getRGB());
    }

    @Override
    public Color deserialize(ObjectSerializer serializer, BaseValue value, Class<?> typeClass) {
        return new Color(((NumberValue) value).getValue().intValue());
    }
}
