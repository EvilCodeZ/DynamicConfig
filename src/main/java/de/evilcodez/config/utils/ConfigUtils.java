package de.evilcodez.config.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.evilcodez.config.BaseValue;
import de.evilcodez.config.ListValue;
import de.evilcodez.config.MapValue;

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
		return c == '0' || c == 'r' || c == 'b' || c == '\\' || c == 't' || c == 'f' || c == 'n' || c == '\'' || c == '\"';
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
		return s.replace("\\", "\\\\")
				.replace("\0", "\\0")
				.replace("\r", "\\r")
				.replace("\b", "\\b")
				.replace("\f", "\\f")
				.replace("\t", "\\t")
				.replace("\n", "\\n")
				.replace("\"", "\\\"")
				.replace("'", "\\'");
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
}
