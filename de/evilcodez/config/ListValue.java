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
	
	public StringValue addString(String value) {
		final StringValue val = new StringValue(value);
		this.elements.add(val);
		return val;
	}
	
	public NumberValue addNumber(Number value) {
		final NumberValue val = new NumberValue(value);
		this.elements.add(val);
		return val;
	}
	
	public CharValue addChar(char value) {
		final CharValue val = new CharValue(value);
		this.elements.add(val);
		return val;
	}
	
	public BooleanValue addBoolean(boolean value) {
		final BooleanValue val = new BooleanValue(value);
		this.elements.add(val);
		return val;
	}
	
	public BaseValue add(BaseValue value) {
		this.elements.add(value);
		return value;
	}
	
	public BaseValue get(int index) {
		return this.elements.get(index);
	}
	
	public String getString(int index) {
		try {
			return ((StringValue) this.elements.get(index)).getValue();
		}catch(ClassCastException ignored) {}
		return "";
	}
	
	public Number getNumber(int index) {
		try {
			return ((NumberValue) this.elements.get(index)).getValue();
		}catch(ClassCastException ignored) {}
		return new Integer(0);
	}
	
	public char getChar(int index) {
		try {
			return ((CharValue) this.elements.get(index)).getValue();
		}catch(ClassCastException ignored) {}
		return 0;
	}
	
	public boolean getBoolean(int index) {
		try {
			return ((BooleanValue) this.elements.get(index)).getValue();
		}catch(ClassCastException ignored) {}
		return false;
	}
	
	public ListValue getList(int index) {
		try {
			return (ListValue) this.elements.get(index);
		}catch(ClassCastException ignored) {}
		return new ListValue();
	}
	
	public MapValue getMap(int index) {
		try {
			return (MapValue) this.elements.get(index);
		}catch(ClassCastException ignored) {}
		return new MapValue();
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
