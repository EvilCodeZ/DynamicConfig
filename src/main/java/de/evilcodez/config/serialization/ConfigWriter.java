package de.evilcodez.config.serialization;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.evilcodez.config.*;
import de.evilcodez.config.utils.ConfigUtils;

public class ConfigWriter {

	private final String tabString;
	private boolean prettyPrinting;
	private boolean semicolonSeparator;
	private boolean mapColonSeparator;
	private boolean mapStringFields;
	private int tabCount;
	private boolean isWriting;

	public ConfigWriter() {
		this(false);
	}

	public ConfigWriter(boolean prettyPrinting) {
		this("\t", prettyPrinting, false, false, false);
	}

	public ConfigWriter(String tabString, boolean prettyPrinting, boolean semicolonSeparator, boolean mapColonSeparator, boolean mapStringFields) {
		this.tabString = tabString;
		this.prettyPrinting = prettyPrinting;
		this.semicolonSeparator = semicolonSeparator;
		this.mapColonSeparator = mapColonSeparator;
		this.mapStringFields = mapStringFields;
	}

	public ConfigWriter(boolean prettyPrinting, String tabString) {
		this(tabString, prettyPrinting, false, false, false);
	}

	public String serialize(BaseValue value) {
		if (isWriting) {
			throw new RuntimeException("Already writing!");
		}
		isWriting = true;
		this.tabCount = 0;
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
			final NumberValue val = (NumberValue) value;
			if(val.getValue() instanceof Double) {
				if(!Double.isFinite(val.getValue().doubleValue())) {
					throw new IllegalArgumentException("NumberValue is not finite!");
				}
			}else if(val.getValue() instanceof Float) {
				if(!Float.isFinite(val.getValue().floatValue())) {
					throw new IllegalArgumentException("NumberValue is not finite!");
				}
			}
			sb.append(((NumberValue) value).getValue().toString());
		} else if (value instanceof MapValue) {
			sb.append(this.serializeMap(((MapValue) value).getValueMap(), '{', '}'));
		} else if (value instanceof ListValue) {
			sb.append(this.serializeList((ListValue) value));
		} else if (value instanceof BooleanValue) {
			sb.append(((BooleanValue) value).getValue());
		} else if (value instanceof NullValue) {
			sb.append("null");
		} else {
			throw new IllegalArgumentException("Unknown Value type: " + value.getClass().getName());
		}
		return sb.toString();
	}

	private String serializeMap(Map<String, BaseValue> map, char startChar, char endChar) {
		boolean isAttribMap = startChar == '(' && endChar == ')';
		boolean pretty = prettyPrinting && !isAttribMap;
		if (map.isEmpty()) {
			return "" + startChar + endChar;
		}
		final StringBuilder sb = new StringBuilder();
		sb.append(startChar).append(pretty ? System.lineSeparator() : "");
		tabCount++;

		int idx = 0;
		for (String key : map.keySet()) {
			if (pretty) {
				sb.append(tabString());
			}
			if(!ConfigUtils.isValidMapFieldName(key) || mapStringFields) {
				sb.append("\"").append(ConfigUtils.escapeString(key)).append("\"");
			}else {
				sb.append(key);
			}
			if(prettyPrinting && !mapColonSeparator) sb.append(" ");
			sb.append(mapColonSeparator ? ":" : "=");
			if(prettyPrinting) sb.append(" ");
			sb.append(this.serialize0(map.get(key)));
			if (idx < map.size() - 1) {
				final char seperator = semicolonSeparator ? ';' : ',';
				sb.append(prettyPrinting && isAttribMap ? seperator + " " : seperator);
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
			return "[]";
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
				sb.append(semicolonSeparator ? ';' : ',');
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

	public void saveFile(BaseValue value, File file) throws IOException {
		final FileWriter fw = new FileWriter(file);
		fw.write(this.serialize0(value));
		fw.flush();
		fw.close();
	}

	public boolean isMapColonSeparator() {
		return mapColonSeparator;
	}

	public void setMapColonSeparator(boolean mapColonSeparator) {
		this.mapColonSeparator = mapColonSeparator;
	}

	public boolean isPrettyPrinting() {
		return prettyPrinting;
	}

	public void setPrettyPrinting(boolean prettyPrinting) {
		this.prettyPrinting = prettyPrinting;
	}

	public boolean isSemicolonSeparator() {
		return semicolonSeparator;
	}

	public void setSemicolonSeparator(boolean semicolonSeparator) {
		this.semicolonSeparator = semicolonSeparator;
	}

	public boolean isMapStringFields() {
		return mapStringFields;
	}

	public void setMapStringFields(boolean mapStringFields) {
		this.mapStringFields = mapStringFields;
	}
}
