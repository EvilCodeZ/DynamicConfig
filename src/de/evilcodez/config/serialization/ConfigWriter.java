package de.evilcodez.config.serialization;

import java.util.List;
import java.util.Map;

import de.evilcodez.config.BaseValue;
import de.evilcodez.config.BooleanValue;
import de.evilcodez.config.CharValue;
import de.evilcodez.config.ListValue;
import de.evilcodez.config.MapValue;
import de.evilcodez.config.NumberValue;
import de.evilcodez.config.StringValue;
import de.evilcodez.config.utils.ConfigUtils;

public class ConfigWriter {

	private final String tabString;
	private final boolean prettyPrinting;
	private int tabCount;
	private boolean isWriting;

	public ConfigWriter() {
		this.tabString = "\t";
		this.prettyPrinting = false;
	}

	public ConfigWriter(boolean prettyPrinting) {
		this.tabString = "\t";
		this.prettyPrinting = prettyPrinting;
	}

	public ConfigWriter(boolean prettyPrinting, String tabString) {
		this.tabString = tabString;
		this.prettyPrinting = prettyPrinting;
	}

	public String serialize(BaseValue value) {
		if (isWriting) {
			throw new RuntimeException("Already writing!");
		}
		isWriting = true;
		final String result = this.serialize0(value);
		isWriting = false;
		return result;
	}

	private String serialize0(BaseValue value) {
		final StringBuilder sb = new StringBuilder();
		if (!value.getAttributes().isEmpty()) {
			sb.append(this.serializeMap(value.getAttributes(), '(', ')'));
			if (prettyPrinting) {
				sb.append(" ");
			}
		}
		if (value instanceof StringValue) {
			sb.append("\"").append(ConfigUtils.escapeString(((StringValue) value).getValue())).append("\"");
		} else if (value instanceof CharValue) {
			sb.append("'").append(ConfigUtils.escapeString(((CharValue) value).getValue() + "")).append("'");
		} else if (value instanceof NumberValue) {
			sb.append(((NumberValue) value).getValue().toString());
		} else if (value instanceof MapValue) {
			sb.append(this.serializeMap(((MapValue) value).getValueMap(), '{', '}'));
		} else if (value instanceof ListValue) {
			sb.append(this.serializeList((ListValue) value));
		} else if (value instanceof BooleanValue) {
			sb.append(((BooleanValue) value).getValue());
		} else {
			throw new IllegalArgumentException("Unknown Value type: " + value.getClass().getName());
		}
		return sb.toString();
	}

	private String serializeMap(Map<String, BaseValue> map, char startChar, char endChar) {
		boolean isAttribMap = startChar == '(' && endChar == ')';
		boolean pretty = prettyPrinting && !isAttribMap;
		if (map.isEmpty()) {
			return (pretty ? tabString() : "") + startChar + endChar;
		}
		final StringBuilder sb = new StringBuilder();
		sb.append(startChar).append(pretty ? System.lineSeparator() : "");
		tabCount++;

		int idx = 0;
		for (String key : map.keySet()) {
			if (!ConfigUtils.isValidMapFieldName(key)) {
				throw new IllegalArgumentException(
						"Illegal field name in" + (startChar == '(' ? "attribute map" : "map") + ".");
			}
			if (pretty) {
				sb.append(tabString());
			}
			sb.append(key);
			sb.append(prettyPrinting ? " = " : "=");
			sb.append(this.serialize0(map.get(key)));
			if (idx < map.size() - 1) {
				sb.append(prettyPrinting && isAttribMap ? ", " : ",");
			}
			if (pretty) {
				sb.append(System.lineSeparator());
			}
			idx++;
		}

		tabCount--;
		if (pretty) {
			sb.append(tabString());
		}
		sb.append(endChar);
		return sb.toString();
	}

	private String serializeList(ListValue value) {
		boolean b = prettyPrinting;
		if (value.getElements().isEmpty()) {
			return (b ? tabString() : "") + "[]";
		}
		final StringBuilder sb = new StringBuilder();
		sb.append("[").append(b ? System.lineSeparator() : "");
		tabCount++;
		final List<BaseValue> list = value.getElements();

		for (int i = 0; i < list.size(); i++) {
			if (b) {
				sb.append(tabString());
			}
			sb.append(this.serialize0(list.get(i)));
			if (i < list.size() - 1) {
				sb.append(",");
			}
			if (b) {
				sb.append(System.lineSeparator());
			}
		}

		tabCount--;
		if (b) {
			sb.append(tabString());
		}
		sb.append("]");
		return sb.toString();
	}

	private String tabString() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tabCount; i++) {
			sb.append(tabString);
		}
		return sb.toString();
	}
}
