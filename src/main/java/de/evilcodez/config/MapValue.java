package de.evilcodez.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.evilcodez.config.utils.ConfigUtils;

public class MapValue extends BaseValue {
	
	private final Map<String, BaseValue> valueMap;
	
	public MapValue() {
		this.valueMap = new HashMap<>();
	}
	
	public MapValue(Map<String, BaseValue> valueMap) {
		this.valueMap = valueMap;
	}
	
	public MapValue getMap(String key) {
		try {
			return (MapValue) valueMap.get(key);
		}catch(ClassCastException ignored) {
		}
		return null;
	}
	
	public BaseValue setMap(String key, MapValue map) {
		valueMap.put(key, map);
		return map;
	}
	
	public ListValue getList(String key) {
		try {
			return (ListValue) valueMap.get(key);
		}catch(ClassCastException ignored) {
		}
		return null;
	}
	
	public ListValue setList(String key, ListValue list) {
		valueMap.put(key, list);
		return list;
	}
	
	public String getString(String key) {
		try {
			return ((StringValue) valueMap.get(key)).getValue();
		}catch(ClassCastException ignored) {
		}
		return "";
	}
	
	public BaseValue setString(String key, String value) {
		StringValue val = new StringValue(value);
		valueMap.put(key, val);
		return val;
	}
	
	public char getChar(String key) {
		try {
			return ((CharValue) valueMap.get(key)).getValue();
		}catch(ClassCastException ignored) {
		}
		return 0;
	}
	
	public BaseValue setChar(String key, char value) {
		CharValue val = new CharValue(value);
		valueMap.put(key, val);
		return val;
	}
	
	public Number getNumber(String key) {
		try {
			return ((NumberValue) valueMap.get(key)).getValue();
		}catch(ClassCastException ignored) {
		}
		return new Integer(0);
	}
	
	public BaseValue setNumber(String key, Number value) {
		NumberValue val = new NumberValue(value);
		valueMap.put(key, val);
		return val;
	}
	
	public boolean getBoolean(String key) {
		try {
			return ((BooleanValue) valueMap.get(key)).getValue();
		}catch(ClassCastException ignored) {
		}
		return false;
	}
	
	public BaseValue setBoolean(String key, boolean value) {
		BooleanValue val = new BooleanValue(value);
		valueMap.put(key, val);
		return val;
	}
	
	public BaseValue get(String key) {
		return valueMap.get(key);
	}
	
	public BaseValue set(String key, BaseValue value) {
		BaseValue val = value == null ? new NullValue() : value;
		valueMap.put(key, val);
		return val;
	}
	
	public boolean has(String key) {
		return this.valueMap.containsKey(key);
	}
	
	public boolean isClassOfChild(String key, Class<? extends BaseValue> childClass) {
		if(!has(key)) {
			return false;
		}
		return get(key).getClass() == childClass;
	}

	public boolean isNull(String key) {
		return has(key) && get(key) instanceof NullValue;
	}
	
	public Map<String, BaseValue> getValueMap() {
		return valueMap;
	}
	
	public Set<Entry<String, BaseValue>> entrySet() {
		return valueMap.entrySet();
	}
	
	public int size() {
		return valueMap.size();
	}
	
	@Override
	public BaseValue copy() {
		final MapValue copy = new MapValue();
		copy.setAttributes(ConfigUtils.copyAttributes(this));
		copy.getValueMap().putAll(ConfigUtils.copyMapValue(this));
		return copy;
	}

	@Override
	public boolean equals(Object object) {
		if(object == this) {
			return true;
		}
		if(!(object instanceof MapValue)) {
			return false;
		}
		final MapValue val = (MapValue) object;
		if(!ConfigUtils.areAttributesEqual(this, val)) {
			return false;
		}
		return ConfigUtils.isMapContentEqual(this, val);
	}

	@Override
	public String toString() {
		return ConfigUtils.attributesToString(attributes) + "MapValue" + valueMap;
	}
}
