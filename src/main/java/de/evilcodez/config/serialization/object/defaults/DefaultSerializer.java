package de.evilcodez.config.serialization.object.defaults;

import de.evilcodez.config.BaseValue;
import de.evilcodez.config.ListValue;
import de.evilcodez.config.MapValue;
import de.evilcodez.config.NullValue;
import de.evilcodez.config.serialization.object.*;
import de.evilcodez.config.utils.SilentObjectCreator;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class DefaultSerializer implements TypeSerializer<Object> {

    @Override
    public BaseValue serialize(ObjectSerializer serializer, Object value, Class<?> typeClass) {
        if(value == null) {
            return new NullValue();
        }
        if(typeClass.isArray()) {
            return this.serializeArray(serializer, value);
        }
        return this.serializeObject(serializer, value, typeClass);
    }

    private BaseValue serializeArray(ObjectSerializer serializer, Object value) {
        final ListValue list = new ListValue();
        int len = Array.getLength(value);
        for(int i = 0; i < len; i++) {
            final Object val = Array.get(value, i);
            if(val == null) {
                list.add(new NullValue());
                continue;
            }
            final TypeSerializer s = serializer.getSerializerForClass(val.getClass());
            list.add(s.serialize(serializer, val, val.getClass()));
        }
        return list;
    }

    private BaseValue serializeObject(ObjectSerializer serializer, Object value, Class<?> typeClass) {
        final MapValue map = new MapValue();

        Class<?> clazz = typeClass;
        while(clazz != null) {
            for(int i = 0; i < clazz.getDeclaredFields().length; i++) {
                final Field field = clazz.getDeclaredFields()[i];
                if((field.getModifiers() & Modifier.STATIC) != 0) {
                    continue;
                }
                if(field.isAnnotationPresent(SkipFieldSerialization.class)
                        && field.getAnnotation(SkipFieldSerialization.class).skipSerialization()) {
                    continue;
                }
                String name = field.getName();
                if(field.isAnnotationPresent(SerializedFieldName.class)) {
                    name = field.getAnnotation(SerializedFieldName.class).value();
                }
                map.set(name, this.serializeField(serializer, value, field));
            }
            clazz = clazz.getSuperclass();
        }

        return map;
    }

    private BaseValue serializeField(ObjectSerializer serializer, Object value, Field field) {
        try {
            boolean wasAccessible = field.isAccessible();
            field.setAccessible(true);
            final Object val = field.get(value);
            if(val == null) {
                return new NullValue();
            }
            field.setAccessible(wasAccessible);
            final TypeSerializer s = serializer.getSerializerForClass(val.getClass());
            return s.serialize(serializer, val, val.getClass());
        }catch(Exception ex) {
            throw new RuntimeException("Failed to serialize field!", ex);
        }
    }

    @Override
    public Object deserialize(ObjectSerializer serializer, BaseValue value, Class<?> typeClass) {
        if(value instanceof NullValue) {
            return null;
        }
        if(typeClass.isArray()) {
            return this.deserializeArray(serializer, value, typeClass);
        }
        return this.deserializeObject(serializer, value, typeClass);
    }

    private Object deserializeArray(ObjectSerializer serializer, BaseValue value, Class<?> typeClass) {
        final ListValue list = (ListValue) value;
        final Object array = Array.newInstance(typeClass.getComponentType(), list.size());
        final Class<?> clazz = this.primitiveToObjectClass(typeClass.getComponentType());
        final TypeSerializer s = serializer.getSerializerForClass(clazz);
        for(int i = 0; i < list.size(); i++) {
            Array.set(array, i, s.deserialize(serializer, list.get(i), clazz));
        }
        return array;
    }

    private Object deserializeObject(ObjectSerializer serializer, BaseValue value, Class<?> typeClass) {
        final MapValue map = (MapValue) value;
        final Object obj = SilentObjectCreator.create(typeClass);
        Class<?> clazz = typeClass;
        while(clazz != null) {
            for(int i = 0; i < clazz.getDeclaredFields().length; i++) {
                final Field field = clazz.getDeclaredFields()[i];
                if((field.getModifiers() & Modifier.STATIC) != 0) {
                    continue;
                }
                if(field.isAnnotationPresent(SkipFieldSerialization.class)
                        && field.getAnnotation(SkipFieldSerialization.class).skipDeserialization()) {
                    continue;
                }
                String name = field.getName();
                if(field.isAnnotationPresent(SerializedFieldName.class)) {
                    name = field.getAnnotation(SerializedFieldName.class).value();
                }
                if(map.has(name)) {
                    this.deserializeField(serializer, map.get(name), obj, field);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return obj;
    }

    private void deserializeField(ObjectSerializer serializer, BaseValue value, Object obj, Field field) {
        try {
            boolean wasAccessible = field.isAccessible();
            field.setAccessible(true);
            final Class<?> clazz = primitiveToObjectClass(field.getType());
            final TypeSerializer s = serializer.getSerializerForClass(clazz);
            if(s == null) {
                throw new RuntimeException("No TypeSerializer found for type: " + clazz.getName());
            }
            field.set(obj, s.deserialize(serializer, value, clazz));
            field.setAccessible(wasAccessible);
        }catch(Exception ex) {
            throw new RuntimeException("Failed to deserialize field!", ex);
        }
    }

    private Class<?> primitiveToObjectClass(Class<?> primitiveClass) {
        if(primitiveClass == byte.class) {
            return Byte.class;
        }else if(primitiveClass == short.class) {
            return Short.class;
        }else if(primitiveClass == int.class) {
            return Integer.class;
        }else if(primitiveClass == long.class) {
            return Long.class;
        }else if(primitiveClass == float.class) {
            return Float.class;
        }else if(primitiveClass == double.class) {
            return Double.class;
        }else if(primitiveClass == char.class) {
            return Character.class;
        }else if(primitiveClass == boolean.class) {
            return Boolean.class;
        }else {
            return primitiveClass;
        }
    }
}
