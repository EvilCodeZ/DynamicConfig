package de.evilcodez.config.serialization.object.defaults;

import de.evilcodez.config.BaseValue;
import de.evilcodez.config.ListValue;
import de.evilcodez.config.NullValue;
import de.evilcodez.config.serialization.object.ObjectSerializer;
import de.evilcodez.config.serialization.object.TypeSerializer;

import java.util.ArrayList;
import java.util.Arrays;

public class ArrayListSerializer implements TypeSerializer<ArrayList> {

    @Override
    public BaseValue serialize(ObjectSerializer serializer, ArrayList value, Class<?> typeClass) {
        final ListValue list = new ListValue();
        for(int i = 0; i < value.size(); i++) {
            final Object obj = value.get(i);
            if(obj == null) {
                list.add(new NullValue());
                continue;
            }
            final TypeSerializer s = serializer.getSerializerForClass(obj.getClass());
            list.add(s.serialize(serializer, obj, obj.getClass()));
        }
        return list;
    }

    @Override
    public ArrayList deserialize(ObjectSerializer serializer, BaseValue value, Class<?> typeClass) {
        if(value instanceof NullValue) {
            return null;
        }
        final BaseValue[] list = serializer.deserialize(value, BaseValue[].class);
        return new ArrayList(Arrays.asList(list));
    }
}
