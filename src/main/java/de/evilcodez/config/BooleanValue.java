package de.evilcodez.config;

import de.evilcodez.config.utils.ConfigUtils;

public class BooleanValue extends BaseValue {
	
	private boolean value;
	
	public BooleanValue() {
		this.value = false;
	}
	
	public BooleanValue(boolean value) {
		this.value = value;
	}
	
	public boolean getValue() {
		return value;
	}
	
	public void setValue(boolean value) {
		this.value = value;
	}
	
	@Override
	public BaseValue copy() {
		final BooleanValue copy = new BooleanValue(value);
		copy.setAttributes(ConfigUtils.copyAttributes(this));
		return copy;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object == this) {
			return true;
		}
		if(!(object instanceof BooleanValue)) {
			return false;
		}
		if(!ConfigUtils.areAttributesEqual(this, (BooleanValue) object)) {
			return false;
		}
		return value == ((BooleanValue) object).value;
	}
	
	@Override
	public String toString() {
		return ConfigUtils.attributesToString(attributes) + "BooleanValue{" + value + "}";
	}
}
