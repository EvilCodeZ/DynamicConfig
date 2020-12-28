package de.evilcodez.config.serialization.object;

import de.evilcodez.config.BaseValue;
import de.evilcodez.config.NullValue;
import de.evilcodez.config.serialization.object.defaults.*;

import java.awt.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ObjectSerializer {

    private final HashMap<Class<?>, TypeSerializer<?>> serializerMap;

    public ObjectSerializer() {
        this.serializerMap = new HashMap<>();
        this.registerTypeSerializer(Object.class, new DefaultSerializer());
        this.registerTypeSerializer(Enum.class, new EnumSerializer());
        this.registerTypeSerializer(Number.class, new NumberSerializer());
        this.registerTypeSerializer(String.class, new StringSerializer());
        this.registerTypeSerializer(Character.class, new CharacterSerializer());
        this.registerTypeSerializer(Boolean.class, new BooleanSerializer());
        this.registerTypeSerializer(BaseValue.class, new BaseValueSerializer());
        this.registerTypeSerializer(ArrayList.class, new ArrayListSerializer());
        this.registerTypeSerializer(UUID.class, new UUIDSerializer());
        this.registerTypeSerializer(InetAddress.class, new InetAddressSerializer());
        this.registerTypeSerializer(Color.class, new ColorSerializer());
    }

    public <T> BaseValue serialize(T value) {
        return this.serialize(value, false);
    }

    public <T> BaseValue serialize(T value, boolean enableReferences) {
        if(value == null) {
            return new NullValue();
        }
        final TypeSerializer<T> serializer = this.getSerializerForClass(value.getClass());
        if(serializer == null) {
            throw new NullPointerException("TypeSerializer for type " + value.getClass().getName() + " not found!");
        }
        final SerializationContext ctx = new SerializationContext(enableReferences);
        return serializer.serialize(this, ctx, null, value, value.getClass());
    }

    public <T> T deserialize(BaseValue value, Class<T> typeClass) {
        return this.deserialize(value, typeClass, false);
    }

    public <T> T deserialize(BaseValue value, Class<T> typeClass, boolean enableReferences) {
        if(value instanceof NullValue) {
            return null;
        }
        final TypeSerializer<T> serializer = this.getSerializerForClass(typeClass);
        if(serializer == null) {
            throw new NullPointerException("TypeSerializer for type " + value.getClass().getName() + " not found!");
        }
        final SerializationContext ctx = new SerializationContext(enableReferences);
        ctx.put(DefaultSerializer.ROOT_OBJECT_KEY, value);
        final T val = serializer.deserialize(this, ctx, null, value, typeClass);
        serializer.postDeserialize(this, ctx, null, val);
        return val;
    }

    public void registerTypeSerializer(Class<?> typeClass, TypeSerializer<?> serializer) {
        this.serializerMap.put(typeClass, serializer);
    }

    public <T> TypeSerializer<T> getSerializerForClass(Class<?> typeClass) {
        if(typeClass == null) {
            return this.getSerializerForClass(Object.class);
        }
        TypeSerializer<T> serializer;
        Class<?> clazz = typeClass;
        while((serializer = (TypeSerializer<T>) this.serializerMap.get(clazz)) == null && clazz != null) {
            clazz = clazz.getSuperclass();
        }
        return serializer;
    }
}
