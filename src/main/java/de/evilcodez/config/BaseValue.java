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
}
