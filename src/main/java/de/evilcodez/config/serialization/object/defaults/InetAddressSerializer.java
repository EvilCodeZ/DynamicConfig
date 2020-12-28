package de.evilcodez.config.serialization.object.defaults;

import de.evilcodez.config.BaseValue;
import de.evilcodez.config.StringValue;
import de.evilcodez.config.serialization.object.ObjectSerializer;
import de.evilcodez.config.serialization.object.SerializationContext;
import de.evilcodez.config.serialization.object.TypeSerializer;
import de.evilcodez.config.serialization.object.path.ValuePath;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetAddressSerializer implements TypeSerializer<InetAddress> {

    @Override
    public BaseValue serialize(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, InetAddress value, Class<?> typeClass) {
        return new StringValue(value.getHostAddress());
    }

    @Override
    public InetAddress deserialize(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, BaseValue value, Class<?> typeClass) {
        try {
            return InetAddress.getByName(((StringValue) value).getValue());
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed to get InetAddress!", e);
        }
    }
}
