package de.evilcodez.config;

import de.evilcodez.config.utils.ConfigUtils;

public class NullValue extends BaseValue {

    @Override
    public BaseValue copy() {
        return new NullValue();
    }

    @Override
    public boolean equals(Object object) {
        if(object == null) {
            return false;
        }
        return object.getClass() == NullValue.class;
    }

    @Override
    public String toString() {
        return ConfigUtils.attributesToString(attributes) + "NullValue{}";
    }
}
