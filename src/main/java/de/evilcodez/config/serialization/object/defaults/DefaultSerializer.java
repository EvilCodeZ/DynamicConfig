package de.evilcodez.config.serialization.object.defaults;

import de.evilcodez.config.*;
import de.evilcodez.config.serialization.object.*;
import de.evilcodez.config.serialization.object.path.ListValuePath;
import de.evilcodez.config.serialization.object.path.MapValuePath;
import de.evilcodez.config.serialization.object.path.ValuePath;
import de.evilcodez.config.utils.ConfigUtils;
import de.evilcodez.config.utils.SilentObjectCreator;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DefaultSerializer implements TypeSerializer<Object> {

    public static final Object ROOT_OBJECT_KEY = new Object();
    public static final Object CHILD_OBJECT_KEY = new Object();
    public static final Object REF_LIST_KEY = new Object();

    @Override
    public BaseValue serialize(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, Object value, Class<?> typeClass) {
        if(value == null) {
            return new NullValue();
        }

        if(!ctx.has(CHILD_OBJECT_KEY)) {
            ctx.put(CHILD_OBJECT_KEY, new HashMap<Object, ValuePath>());
        }
        final Map<Object, ValuePath> duplicates = ctx.get(CHILD_OBJECT_KEY);
        duplicates.put(value, path);

        if(typeClass.isArray()) {
            return this.serializeArray(serializer, ctx, path, value);
        }
        return this.serializeObject(serializer, ctx, path, value, typeClass);
    }

    private BaseValue serializeArray(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, Object value) {
        final ListValue list = new ListValue();
        int len = Array.getLength(value);
        for(int i = 0; i < len; i++) {
            final Object val = Array.get(value, i);
            if(val == null) {
                list.add(new NullValue());
                continue;
            }

            final ListValuePath subPath = new ListValuePath(i, path);
            if(ctx.isAllowReferences()) {
                final Map<Object, ValuePath> duplicates = ctx.get(CHILD_OBJECT_KEY);
                if (duplicates.containsKey(val)) {
                    final NullValue nullVal = new NullValue();
                    nullVal.setAttribute("reference", new StringValue(ValuePath.toString(duplicates.get(val))));
                    list.add(nullVal);
                    continue;
                }
            }

            final TypeSerializer s = serializer.getSerializerForClass(val.getClass());
            list.add(s.serialize(serializer, ctx, subPath, val, val.getClass()));
        }
        return list;
    }

    private BaseValue serializeObject(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, Object value, Class<?> typeClass) {
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
                map.set(name, this.serializeField(serializer, ctx, path, name, value, field));
            }
            clazz = clazz.getSuperclass();
        }

        return map;
    }

    private BaseValue serializeField(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, String key, Object value, Field field) {
        try {
            boolean wasAccessible = field.isAccessible();
            field.setAccessible(true);
            final Object val = field.get(value);
            if(val == null) {
                return new NullValue();
            }
            field.setAccessible(wasAccessible);

            final MapValuePath subPath = new MapValuePath(key, path);

            if(ctx.isAllowReferences()) {
                final Map<Object, ValuePath> duplicates = ctx.get(CHILD_OBJECT_KEY);
                if (duplicates.containsKey(val)) {
                    final NullValue nullVal = new NullValue();
                    nullVal.setAttribute("reference", new StringValue(ValuePath.toString(duplicates.get(val))));
                    return nullVal;
                }
            }

            final TypeSerializer s = serializer.getSerializerForClass(val.getClass());
            return s.serialize(serializer, ctx, subPath, val, val.getClass());
        }catch(Exception ex) {
            throw new RuntimeException("Failed to serialize field!", ex);
        }
    }

    @Override
    public Object deserialize(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, BaseValue value, Class<?> typeClass) {
        if(value instanceof NullValue) {
            return null;
        }

        if(!ctx.has(CHILD_OBJECT_KEY)) {
            ctx.put(CHILD_OBJECT_KEY, new HashMap<String, ReferenceTuple>());
        }

        if(typeClass.isArray()) {
            return this.deserializeArray(serializer, ctx, path, value, typeClass);
        }
        return this.deserializeObject(serializer, ctx, path, value, typeClass);
    }

    private Object deserializeArray(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, BaseValue value, Class<?> typeClass) {
        final ListValue list = (ListValue) value;
        final Object array = Array.newInstance(typeClass.getComponentType(), list.size());
        final Class<?> clazz = this.primitiveToObjectClass(typeClass.getComponentType());
        final TypeSerializer s = serializer.getSerializerForClass(clazz);
        for(int i = 0; i < list.size(); i++) {
            final int idx = i;
            BaseValue val = list.get(i);
            final ListValuePath subPath = new ListValuePath(i, path);
            if(ctx.isAllowReferences() && val instanceof NullValue && val.hasAttribute("references")) {
                val = ConfigUtils.applyPath(ctx.get(ROOT_OBJECT_KEY), ConfigUtils.createPath(((StringValue) val.getAttribute("references")).getValue()));
            }
            Object ref = this.createObject(serializer, ctx, subPath, clazz, val,
                    (v) -> Array.set(array, idx, v));
            if(!ctx.isAllowReferences()) {
                Array.set(array, i, ref);
            }
        }
        return array;
    }

    private Object deserializeObject(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, BaseValue value, Class<?> typeClass) {
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
                    final BaseValue val = map.get(name);
                    this.deserializeField(serializer, ctx, new MapValuePath(name, path), val, obj, field);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return obj;
    }

    private void deserializeField(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, BaseValue value, Object obj, Field field) {
        if(ctx.isAllowReferences() && value instanceof NullValue && value.hasAttribute("reference")) {
            value = ConfigUtils.applyPath(ctx.get(ROOT_OBJECT_KEY),
                    ConfigUtils.createPath(((StringValue) value.getAttribute("reference")).getValue()));
        }
        final Object ref = this.createObject(serializer, ctx, path, field.getType(), value, (v) -> this.setField(field, obj, v));
        if(!ctx.isAllowReferences()) {
            this.setField(field, obj, ref);
        }
    }

    private Object createObject(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, Class<?> typeClass, BaseValue value, ValueWriter writer) {
        if(value instanceof NullValue && !value.hasAttribute("reference")) {
            return null;
        }

        final Class<?> clazz = primitiveToObjectClass(typeClass);
        final TypeSerializer s = serializer.getSerializerForClass(clazz);
        if(s == null) {
            throw new RuntimeException("No TypeSerializer found for type: " + clazz.getName());
        }

        Object ref = null;
        if(ctx.isAllowReferences()) {
            final Map<String, ReferenceTuple> referenceMap = ctx.get(CHILD_OBJECT_KEY);
            final String pathstr = ValuePath.toString(path);
            if (!referenceMap.containsKey(pathstr)) {
                final ReferenceTuple tuple = new ReferenceTuple(writer, null);
                referenceMap.put(pathstr, tuple);
                ref = s.deserialize(serializer, ctx, path, value, clazz);
                tuple.value = ref;
                ref = null;
            }
        }else {
            ref = s.deserialize(serializer, ctx, path, value, clazz);
        }
        return ref;
    }

    private void setField(Field field, Object instance, Object value) {
        try {
            boolean wasAccessible = field.isAccessible();
            field.setAccessible(true);
            field.set(instance, value);
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

    @Override
    public void postDeserialize(ObjectSerializer serializer, SerializationContext ctx, ValuePath path, Object value) {
        if(!ctx.isAllowReferences()) {
            return;
        }
        ctx.<Map<ValuePath, ReferenceTuple>>get(CHILD_OBJECT_KEY).forEach((pathNode, tuple) -> {
//            System.out.println(pathNode + " - " + (tuple.value == null));
            tuple.accessor.set(tuple.value);
        });
    }

    private static class ReferenceTuple {
        private ValueWriter accessor;
        private Object value;

        public ReferenceTuple(ValueWriter accessor, Object value) {
            this.accessor = accessor;
            this.value = value;
        }
    }

    private static interface ValueWriter {
        void set(Object value);
    }
}
