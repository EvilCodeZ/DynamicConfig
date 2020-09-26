package de.evilcodez.config;

import de.evilcodez.config.utils.ConfigUtils;

public class StringValue extends BaseValue {
	
	private String value;
	
	public StringValue() {
		this.value = "";
	}
	
	public StringValue(String value) {
		this.value = value;
	}
	
	@Override
	public BaseValue copy() {
		final StringValue copy = new StringValue();
		copy.setAttributes(ConfigUtils.copyAttributes(this));
		copy.setValue(value);
		return copy;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object == this) {
			return true;
		}
		if(!(object instanceof StringValue)) {
			return false;
		}
		if(!ConfigUtils.areAttributesEqual(this, (StringValue) object)) {
			return false;
		}
		return value.equals(((StringValue) object).value);
	}
	
	@Override
	public String toString() {
		return ConfigUtils.attributesToString(attributes) + "StringValue{" + value + "}";
	}
}
