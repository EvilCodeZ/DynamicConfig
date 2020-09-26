package de.evilcodez.config.serialization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

public class ConfigParser {
	
	private int index;
	private int line;
	private boolean isParsing;
	
	public ConfigParser() {
		this.index = 0;
		this.line = 1;
	}
	
	public BaseValue parse(String content) {
		if(isParsing) {
			throw new RuntimeException("Already parsing!");
		}
		isParsing = true;
		index = 0;
		line = 1;
		try {
			final char[] chars = content.toCharArray();
			final BaseValue value = this.parse0(chars);
			isParsing = false;
			return value;
		}catch(Exception ex) {
			isParsing = false;
			throw ex;
		}
	}
	
	private BaseValue parse0(char[] content) {
		this.skipWhiteSpaces(content);
		
		Map<String, BaseValue> attributes = null;
		if(content[index] == '(') {
			attributes = this.parseMap(content, '(', ')').getValueMap();
			index++;
			this.skipWhiteSpaces(content);
		}else {
			attributes = new HashMap<>();
		}
		BaseValue value = null;
		
		final char c = content[index];
		if(c == '{') {
			value = this.parseMap(content, '{', '}');
		}else if(c == '[') {
			value = this.parseList(content);
		}else if(c == '"') {
			value = this.parseString(content);
		}else if(c == '\'') {
			value = this.parseCharacter(content);
		}else if(c == '-' || ConfigUtils.isDigit(c)) {
			value = this.parseInt(content);
		}else if(c == 't' || c == 'f') {
			value = this.parseBoolean(content);
		}else {
			throw new SyntaxException(line, "Not expected '" + c + "'");
		}
		value.setAttributes(attributes);
		return value;
	}
	
	private ListValue parseList(char[] content) {
		if(content[index] != '[') {
			throw new SyntaxException(line, "Expected character '{' not found!");
		}
		index++;
		assertEOF(content);
		final List<BaseValue> list = new ArrayList<BaseValue>();
		while(true) {
			assertEOF(content);
			this.skipWhiteSpaces(content);
			char c = content[index];
			if(c == ',' || c == ';') {
				index++;
				continue;
			}
			
			if(c == ']') {
				break;
			}
			
			list.add(this.parse0(content));
			index++;
		}
		return new ListValue(list);
	}
	
	private MapValue parseMap(char[] content, char startChar, char endChar) {
		if(content[index] != startChar) {
			throw new SyntaxException(line, "Expected character '" + startChar + "' not found!");
		}
		index++;
		assertEOF(content);
		final Map<String, BaseValue> map = new HashMap<String, BaseValue>();
		boolean parsingField = false;
		int fieldNameStart = -1;
		String fieldName = null;
		while(true) {
			assertEOF(content);
			if(parsingField && !ConfigUtils.isValidMapKeyCharacter(content[index])) {
				fieldName = new String(Arrays.copyOfRange(content, fieldNameStart, index));
			}
			this.skipWhiteSpaces(content);
			char c = content[index];
			
			if(c == endChar) {
				break;
			}
			
			if(!parsingField && (c == ',' || c == ';')) {
				index++;
				continue;
			}
			
			if(c == '=' && parsingField) {
				if(!ConfigUtils.isValidMapFieldName(fieldName)) {
					throw new SyntaxException(line, "Field name '" + fieldName + "' is not valid.");
				}
				index++;
				this.skipWhiteSpaces(content);
				if(map.containsKey(fieldName)) {
					throw new SyntaxException(line, "Duplicate field \"" + fieldName + "\".");
				}
				map.put(fieldName, this.parse0(content));
				parsingField = false;
				index++;
				continue;
			}else if(c == '=' && !parsingField) {
				throw new SyntaxException(line, "Unexpected character '='.");
			}
			if(!parsingField && ConfigUtils.isValidMapKeyCharacter(c)) {
				parsingField = true;
				fieldNameStart = index;
			}else if(!ConfigUtils.isValidMapKeyCharacter(c)) {
				if(parsingField) {
					throw new SyntaxException(line, "Unexpected character '" + c + "' in field name at " + new String(Arrays.copyOfRange(content, fieldNameStart, index)) + ".");
				}else {
					throw new SyntaxException(line, "Unexpected character '" + c + "'.");
				}
			}
			
			index++;
		}
		return new MapValue(map);
	}
	
	private NumberValue parseInt(char[] content) {
		final StringBuilder sb = new StringBuilder();
		if(content[index] == '-') {
			sb.append('-');
			index++;
		}
		boolean isFloat = false;
		boolean hasE = false;
		while(true) {
			if(index >= content.length) {
				break;
			}
			assertEOF(content);
			char c = content[index];
			if(c == 'E' && !hasE) {
				hasE = true;
				index++;
				continue;
			}else if(c == 'e' && hasE) {
				throw new SyntaxException(line, "Unexpected character in number: " + c);
			}
			if(c != '.' && !ConfigUtils.isDigit(c)) {
				break;
			}else if(c == '.') {
				isFloat = true;
			}
			sb.append(c);
			index++;
		}
		Number val = null;
		try {
			if(isFloat) {
				val = Double.parseDouble(sb.toString());
			}else {
				try {
					val = Integer.parseInt(sb.toString());
				}catch(NumberFormatException ignored) {
					val = Long.parseLong(sb.toString());
				}
			}
		}catch(NumberFormatException ignored) {
			throw new SyntaxException(line, "Failed to parse number.");
		}
		index--;
		return new NumberValue(val);
	}
	
	private BooleanValue parseBoolean(char[] content) {
		if(content[index] != 'f' && content[index] != 't') {
			throw new SyntaxException(line, "Expected a boolean value (true/false).");
		}
		int start = index;
		while(true) {
			assertEOF(content);
			char c = content[index];
			if(c == 'e') {
				break;
			}
			index++;
		}
		final String result = new String(Arrays.copyOfRange(content, start, index + 1));
		if(!result.equalsIgnoreCase("true") && !result.equalsIgnoreCase("false")) {
			throw new SyntaxException(line, "Not a boolean value (true/false): " + result);
		}
		return new BooleanValue(Boolean.valueOf(result).booleanValue());
	}
	
	private StringValue parseString(char[] content) {
		if(content[index] != '"') {
			throw new SyntaxException(line, "Expected character '\"' not found!");
		}
		int start = ++index;
		boolean escaping = false;
		while(true) {
			assertEOF(content);
			char c = content[index];
			
			if(c == '\r') {
				throw new SyntaxException(line, "Not expected a carriage return in string.");
			}
			if(c == '\n') {
				throw new SyntaxException(line, "Not expected a line seperator in string.");
			}
			
			if(c == '\\' && !escaping) {
				escaping = true;
				index++;
				continue;
			}
			if(escaping && !ConfigUtils.isEscapeCharacter(c)) {
				throw new SyntaxException(line, "Not an escape character '" + c + "' in string at position " + (index - start) + ".");
			}
			
			if(c == '"' && !escaping) {
				break;
			}
			if(escaping) {
				escaping = false;
			}
			
			index++;
		}
		return new StringValue(ConfigUtils.unescapeString(new String(Arrays.copyOfRange(content, start, index))));
	}
	
	private CharValue parseCharacter(char[] content) {
		if(content[index] != '\'') {
			throw new SyntaxException(line, "Expected character ''' not found!");
		}
		int start = ++index;
		boolean escaping = false;
		int len = 0;
		while(true) {
			assertEOF(content);
			char c = content[index];
			if(c == '\r') {
				throw new SyntaxException(line, "Unexpected a carriage return in character.");
			}
			if(c == '\n') {
				throw new SyntaxException(line, "Unexpected a line seperator in character.");
			}
			
			if(c == '\\' && !escaping) {
				escaping = true;
				index++;
				continue;
			}
			if(escaping && !ConfigUtils.isEscapeCharacter(c)) {
				throw new SyntaxException(line, "Not an escape character '" + c + "' in character at position " + (index - start) + ".");
			}
			
			if(c == '\'' && !escaping) {
				break;
			}
			len++;
			if(len > 1) {
				throw new SyntaxException(line, "Can't add more characters in character value.");
			}
			if(escaping) {
				escaping = false;
			}
			
			index++;
		}
		if(len == 0) {
			throw new SyntaxException(line, "Character value can't be empty!");
		}
		return new CharValue(ConfigUtils.unescapeString(new String(Arrays.copyOfRange(content, start, index))).charAt(0));
	}
	
	private void skipWhiteSpaces(char[] content) {
		while(true) {
			assertEOF(content);
			char c = content[index];
			if(c == ' ' || c == '\t' || c == '\r') {
				index++;
				continue;
			}
			if(c == '\n') {
				index++;
				line++;
				continue;
			}
			break;
		}
	}
	
	private void assertEOF(char[] content) {
		if(index >= content.length) {
			throw new SyntaxException(line, "Not expected '<eof>'");
		}
	}
}
