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
	private boolean semicolonSeperator;
	private boolean mapColonSeperator;
	private int tabCount;
	private boolean isWriting;

	public ConfigWriter() {
		this(false);
	}

	public ConfigWriter(boolean prettyPrinting) {
		this(prettyPrinting, false, false);
	}

	public ConfigWriter(boolean prettyPrinting, boolean semicolonSeperator, boolean mapColonSeperator) {
		this.tabString = "\t";
		this.prettyPrinting = prettyPrinting;
		this.semicolonSeperator = semicolonSeperator;
		this.mapColonSeperator = mapColonSeperator;
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
			if (!ConfigUtils.isValidMapFieldName(key)) {
				throw new IllegalArgumentException(
						"Illegal field name " + (startChar == '(' ? "attribute map" : "map") + ": " + key);
			}
			if (pretty) {
				sb.append(tabString());
			}
			sb.append(key);
			if(prettyPrinting && !mapColonSeperator) sb.append(" ");
			sb.append(mapColonSeperator ? ":" : "=");
			if(prettyPrinting) sb.append(" ");
			sb.append(this.serialize0(map.get(key)));
			if (idx < map.size() - 1) {
				final char seperator = semicolonSeperator ? ';' : ',';
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
				sb.append(semicolonSeperator ? ';' : ',');
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

	public boolean isMapColonSeperator() {
		return mapColonSeperator;
	}

	public void setMapColonSeperator(boolean mapColonSeperator) {
		this.mapColonSeperator = mapColonSeperator;
	}

	public boolean isPrettyPrinting() {
		return prettyPrinting;
	}

	public void setPrettyPrinting(boolean prettyPrinting) {
		this.prettyPrinting = prettyPrinting;
	}

	public boolean isSemicolonSeperator() {
		return semicolonSeperator;
	}

	public void setSemicolonSeperator(boolean semicolonSeperator) {
		this.semicolonSeperator = semicolonSeperator;
	}
}
