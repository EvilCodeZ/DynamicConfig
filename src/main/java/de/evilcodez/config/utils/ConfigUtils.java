package de.evilcodez.config.utils;

import de.evilcodez.config.BaseValue;
import de.evilcodez.config.ListValue;
import de.evilcodez.config.MapValue;
import de.evilcodez.config.serialization.SyntaxException;
import de.evilcodez.config.serialization.object.path.ListValuePath;
import de.evilcodez.config.serialization.object.path.MapValuePath;
import de.evilcodez.config.serialization.object.path.ValuePath;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigUtils {
	
	public static Map<String, BaseValue> copyAttributes(BaseValue value) {
		final Map<String, BaseValue> attributes = new HashMap<String, BaseValue>();
		value.getAttributes().forEach((name, attribute) -> attributes.put(name, attribute.copy()));
		return attributes;
	}
	
	public static Map<String, BaseValue> copyMapValue(MapValue value) {
		final Map<String, BaseValue> map = new HashMap<String, BaseValue>();
		value.getValueMap().forEach((name, val) -> map.put(name, val.copy()));
		return map;
	}
	
	public static List<BaseValue> copyListValue(ListValue value) {
		final List<BaseValue> list = new ArrayList<>();
		value.getElements().forEach(element -> list.add(element.copy()));
		return list;
	}
	
	public static boolean areAttributesEqual(BaseValue value1, BaseValue value2) {
		final Map<String, BaseValue> attribs1 = value1.getAttributes();
		final Map<String, BaseValue> attribs2 = value2.getAttributes();
		if(attribs1.isEmpty() && attribs2.isEmpty()) {
			return true;
		}
		if(attribs1.size() != attribs2.size()) {
			return false;
		}
		final List<String> names1 = new ArrayList<>(attribs1.keySet());
		names1.sort(null);
		final List<String> names2 = new ArrayList<>(attribs2.keySet());
		names2.sort(null);
		for(int i = 0; i < names1.size(); i++) {
			final String name1 = names1.get(i);
			final String name2 = names2.get(i);
			if(!name1.equals(name2)) {
				return false;
			}
			if(!attribs1.get(name1).equals(attribs2.get(name2))) {
				return false;
			}
		}
		
		return false;
	}
	
	public static boolean isMapContentEqual(MapValue value1, MapValue value2) {
		final Map<String, BaseValue> content1 = value1.getValueMap();
		final Map<String, BaseValue> content2 = value2.getValueMap();
		if(content1.isEmpty() && content2.isEmpty()) {
			return true;
		}
		if(content1.size() != content2.size()) {
			return false;
		}
		final List<String> names1 = new ArrayList<>(content1.keySet());
		names1.sort(null);
		final List<String> names2 = new ArrayList<>(content2.keySet());
		names2.sort(null);
		for(int i = 0; i < names1.size(); i++) {
			final String name1 = names1.get(i);
			final String name2 = names2.get(i);
			if(!name1.equals(name2)) {
				return false;
			}
			if(!content1.get(name1).equals(content2.get(name2))) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isListContentEqual(ListValue value1, ListValue value2) {
		if(value1.getElements().isEmpty() && value2.getElements().isEmpty()) {
			return true;
		}
		if(value1.getElements().size() != value2.getElements().size()) {
			return false;
		}
		final Comparator<BaseValue> comparator = (v1, v2) -> v1.hashCode() - v2.hashCode();
		final List<BaseValue> list1 = new ArrayList<>(value1.getElements());
		list1.sort(comparator);
		final List<BaseValue> list2 = new ArrayList<>(value2.getElements());
		list2.sort(comparator);
		
		for(int i = 0; i < list1.size(); i++) {
			if(!list1.get(i).equals(list2.get(i))) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isEscapeCharacter(char c) {
		return c == '0' || c == 'r' || c == 'b' || c == '\\' || c == 't' || c == 'f' || c == 'n' || c == '\'' || c == '\"' || c == 'u';
	}
	
	public static boolean isValidMapKeyCharacter(char c) {
		return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
				|| c == 'ö' || c == 'Ö'
				|| c == 'ä' || c == 'Ä'
				|| c == 'ü' || c == 'Ü'
				|| c == 'ß'
				|| c == '_' || c == '-'
				|| c == '"' || c == '\''
				|| c == '.';
	}
	
	public static boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}
	
	public static boolean isValidMapFieldName(String name) {
		for(int i = 0; i < name.length(); i++) {
			if(!isValidMapKeyCharacter(name.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	public static String unescapeString(String s) {
		s = unescapeUnicode(s);
		StringBuilder sb = new StringBuilder();
		boolean escaping = false;
		char[] chars = s.toCharArray();
		for(int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if(c == '\\' && !escaping) {
				escaping = true;
				continue;
			}
			if(escaping && !isEscapeCharacter(c)) {
				throw new RuntimeException("Not an escape character '" + c + "' at position " + i + ".");
			}
			if(escaping) {
				escaping = false;
				if(c == '0') {
					c = '\0';
				}else if(c == 'r') {
					c = '\r';
				}else if(c == 'b') {
					c = '\b';
				}else if(c == 'f') {
					c = '\f';
				}else if(c == 't') {
					c = '\t';
				}else if(c == 'n') {
					c = '\n';
				}
			}
			sb.append(c);
		}
		return sb.toString();
	}
	
	public static String escapeString(String s) {
		return escapeUnicode(s.replace("\\", "\\\\")
				.replace("\0", "\\0")
				.replace("\r", "\\r")
				.replace("\b", "\\b")
				.replace("\f", "\\f")
				.replace("\t", "\\t")
				.replace("\n", "\\n")
				.replace("\"", "\\\"")
				.replace("'", "\\'"));
	}

	public static String escapeUnicode(String s) {
		char[] chars = s.toCharArray();
		final StringBuilder sb = new StringBuilder();

		for(int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if(c >= 32 && c <= 126) {
				sb.append(c);
			}else {
				StringBuilder escapeString = new StringBuilder(Integer.toHexString(c).toUpperCase());
				while(escapeString.length() < 4) {
					escapeString.insert(0, '0');
				}
				sb.append("\\u" + escapeString);
			}
		}

		return sb.toString();
	}

	public static String unescapeUnicode(String s) {
		final Pattern pattern = Pattern.compile("\\\\u([0-9a-fA-F]){4}");
		Matcher matcher = pattern.matcher(s);

		while(matcher.find()) {
			String str = matcher.group();
			s = s.replace(str, String.valueOf((char) Integer.decode("#" + str.substring(2)).intValue()));
		}

		return s;
	}
	
	public static String attributesToString(Map<String, BaseValue> attributes) {
		if(attributes.isEmpty()) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		attributes.forEach((key, value) -> sb.append(key).append("=").append(value).append(", "));
		String result = sb.toString();
		if(result.endsWith(", ")) {
			result = result.substring(0, result.length() - 2);
		}
		return "(" + result + ")";
	}

	public static String parseString(char[] content, int offset) {
		int index = offset;
		if(content[index] != '"') {
			throw new SyntaxException(1, "Expected character '\"' not found!");
		}
		int start = ++index;
		boolean escaping = false;
		while(true) {
			if (index >= content.length) {
				throw new SyntaxException(1, "Not expected '<eof>'");
			}
			char c = content[index];

			if(c == '\r') {
				throw new SyntaxException(1, "Not expected a carriage return in string.");
			}
			if(c == '\n') {
				throw new SyntaxException(1, "Not expected a line seperator in string.");
			}

			if(c == '\\' && !escaping) {
				escaping = true;
				index++;
				continue;
			}
			if(escaping && !ConfigUtils.isEscapeCharacter(c)) {
				throw new SyntaxException(1, "Not an escape character '" + c + "' in string at position " + (index - start) + ".");
			}

			if(c == '"' && !escaping) {
				break;
			}
			if(escaping) {
				escaping = false;
			}

			index++;
		}
		return ConfigUtils.unescapeString(new String(Arrays.copyOfRange(content, start, index)));
	}

	public static BaseValue applyPath(BaseValue root, ValuePath path) {
		final Stack<Object> actions = new Stack<>();
		ValuePath node = path;
		while(node != null) {
			if(node instanceof MapValuePath) {
				actions.push(((MapValuePath) node).getKey());
				actions.push("->");
			}else {
				actions.push(((ListValuePath) node).getIndex());
				actions.push("[");
			}
			node = node.getParentNode();
		}
		BaseValue val = root;
		while(!actions.isEmpty()) {
			final String action = (String) actions.pop();
			switch (action) {
				case "->":
					final String key = (String) actions.pop();
					if(!(val instanceof MapValue)) {
						throw new ClassCastException("Expected a map but got: " + val.getClass().getSimpleName());
					}
					final MapValue map = (MapValue) val;
					if(!map.has(key)) {
						throw new RuntimeException("Key not found: " + key);
					}
					val = map.get(key);
					break;
				case "[":
					final int idx = (int) actions.pop();
					if(!(val instanceof ListValue)) {
						throw new ClassCastException("Expected a list but got: " + val.getClass().getSimpleName());
					}
					final ListValue list = (ListValue) val;
					if(idx < 0 || idx >= list.size()) {
						throw new IndexOutOfBoundsException("List[0 - " + (list.size() - 1) + "] index out bounds: " + idx);
					}
					val = list.get(idx);
					break;
				default:
					throw new RuntimeException("Unexpected action: " + action);
			}
		}
		return val;
	}

	public static ValuePath createPath(String path) {
		final Queue<String> tokens = new ArrayDeque<>();
		if(path.startsWith("root")) {
			path = path.substring("root".length());
		}
		while(!path.isEmpty()) {
			if(path.startsWith("->\"")) {
				path = path.substring("->".length());
				tokens.add("->");
				final String key = ConfigUtils.parseString(path.toCharArray(), 0);
				path = path.substring(2 + key.length());
				tokens.add(key);
			}else if(path.startsWith("[")) {
				path = path.substring(1);
				int idx = 0;
				final StringBuilder sb = new StringBuilder();
				while(true) {
					final char c = path.charAt(idx++);
					if(c == ']') {
						break;
					}
					sb.append(c);
				}
				tokens.add("[");
				tokens.add(sb.toString());
				path = path.substring(sb.length() + 1);
			}else {
				throw new RuntimeException("Unknown token: " + path);
			}
		}
		ValuePath pathNode = null;
		while(!tokens.isEmpty()) {
			final String token = tokens.poll();
			if(token.equals("->")) {
				pathNode = new MapValuePath(tokens.poll(), pathNode);
			}else if(token.equals("[")) {
				pathNode = new ListValuePath(Integer.parseInt(tokens.poll()), pathNode);
			}else {
				throw new RuntimeException("Unknown token: " + token);
			}
		}
		return pathNode;
	}
}
