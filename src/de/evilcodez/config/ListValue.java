package de.evilcodez.config;

import java.util.ArrayList;
import java.util.List;

import de.evilcodez.config.utils.ConfigUtils;

public class ListValue extends BaseValue {
	
	private List<BaseValue> elements;
	
	public ListValue() {
		this.elements = new ArrayList<>();
	}
	
	public ListValue(List<BaseValue> elements) {
		this.elements = elements;
	}

	public List<BaseValue> getElements() {
		return elements;
	}
	
	public void setElements(List<BaseValue> elements) {
		this.elements = elements;
	}
	
	public void addString(String value) {
		this.elements.add(new StringValue(value));
	}
	
	public void addNumber(Number value) {
		this.elements.add(new NumberValue(value));
	}
	
	public void addChar(char value) {
		this.elements.add(new CharValue(value));
	}
	
	public void add(BaseValue value) {
		this.elements.add(value);
	}
	
	@Override
	public BaseValue copy() {
		final ListValue copy = new ListValue();
		copy.setAttributes(ConfigUtils.copyAttributes(this));
		copy.getElements().addAll(ConfigUtils.copyListValue(this));
		return copy;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object == this) {
			return true;
		}
		if(!(object instanceof ListValue)) {
			return false;
		}
		final ListValue val = (ListValue) object;
		if(!ConfigUtils.areAttributesEqual(this, val)) {
			return false;
		}
		return ConfigUtils.isListContentEqual(this, val);
	}
	
	@Override
	public String toString() {
		return ConfigUtils.attributesToString(attributes) + "ListValue" + elements;
	}
}
