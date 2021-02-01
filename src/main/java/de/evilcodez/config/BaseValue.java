package de.evilcodez.config;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseValue {
	
	protected Map<String, BaseValue> attributes;
	
	public BaseValue() {
		this.attributes = new HashMap<>();
	}
	
	public Map<String, BaseValue> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(Map<String, BaseValue> attributes) {
		this.attributes = attributes;
	}
	
	public BaseValue getAttribute(String name) {
		return attributes.get(name);
	}
	
	public void setAttribute(String name, BaseValue value) {
		if(value == null) {
			attributes.remove(name);
			return;
		}
		attributes.put(name, value);
	}
	
	public void removeAttribute(String name) {
		attributes.remove(name);
	}
	
	public boolean hasAttribute(String name) {
		return attributes.containsKey(name);
	}
	
	public abstract BaseValue copy();
	
	public abstract boolean equals(Object object);

	public final String asString() {
		return ((StringValue) this).getValue();
	}

	public final Number asNumber() {
		return ((NumberValue) this).getValue();
	}

	public final int asInt() {
		return this.asNumber().intValue();
	}

	public final long asLong() {
		return this.asNumber().longValue();
	}

	public final float asFloat() {
		return this.asNumber().floatValue();
	}

	public final double asDouble() {
		return this.asNumber().doubleValue();
	}

	public final short asShort() {
		return this.asNumber().shortValue();
	}

	public final byte asByte() {
		return this.asNumber().byteValue();
	}

	public final boolean asBoolean() {
		return ((BooleanValue) this).getValue();
	}

	public final char asChar() {
		return ((CharValue) this).getValue();
	}

	public final MapValue asMap() {
		return (MapValue) this;
	}

	public final ListValue asList() {
		return (ListValue) this;
	}

	public final boolean isNull() {
		return this instanceof NullValue;
	}
}
