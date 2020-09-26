package de.evilcodez.config;

import de.evilcodez.config.utils.ConfigUtils;

public class CharValue extends BaseValue {
	
	private char value;
	
	public CharValue() {
		this.value = '\0';
	}
	
	public CharValue(char value) {
		this.value = value;
	}
	
	@Override
	public BaseValue copy() {
		final CharValue copy = new CharValue(value);
		copy.setAttributes(ConfigUtils.copyAttributes(this));
		return copy;
	}
	
	public char getValue() {
		return value;
	}
	
	public void setValue(char value) {
		this.value = value;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object == this) {
			return true;
		}
		if(!(object instanceof CharValue)) {
			return false;
		}
		if(!ConfigUtils.areAttributesEqual(this, (CharValue) object)) {
			return false;
		}
		return value == ((CharValue) object).value;
	}
	
	@Override
	public String toString() {
		return ConfigUtils.attributesToString(attributes) + "CharValue{" + value + "}";
	}
}
