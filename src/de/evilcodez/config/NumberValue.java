package de.evilcodez.config;

import de.evilcodez.config.utils.ConfigUtils;

public class NumberValue extends BaseValue {
	
	protected Number value;
	
	public NumberValue() {
		this.value = new Integer(0);
	}
	
	public NumberValue(Number value) {
		this.value = value;
	}
	
	public Number getValue() {
		return value;
	}
	
	public void setValue(Number value) {
		this.value = value;
	}

	@Override
	public BaseValue copy() {
		final NumberValue copy = new NumberValue(value);
		copy.setAttributes(ConfigUtils.copyAttributes(this));
		return copy;
	}

	@Override
	public boolean equals(Object object) {
		if(object == this) {
			return true;
		}
		if(!(object instanceof NumberValue)) {
			return false;
		}
		if(!ConfigUtils.areAttributesEqual(this, (NumberValue) object)) {
			return false;
		}
		return value.equals(((NumberValue) object).value);
	}
	
	@Override
	public String toString() {
		return ConfigUtils.attributesToString(attributes) + "NumberValue{" + value + "}";
	}
}
