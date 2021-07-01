package de.evilcodez.config.serialization;

import de.evilcodez.config.*;
import de.evilcodez.config.utils.ConfigUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ConfigParser {

	private int index;
	private int line;
	private int depth;
	private int maxDepth;
	private boolean isParsing;

	/**
	 * @param maxDepth set to 0 to disable depth check.
	 */
	public ConfigParser(int maxDepth) {
		this.maxDepth = maxDepth;
		this.index = 0;
		this.line = 1;
		this.depth = 0;
	}

	/**
	 * Creates a new parser with default depth check (512).
	 */
	public ConfigParser() {
		this(512);
	}

	public BaseValue parse(String content) {
		if(isParsing) {
			throw new RuntimeException("Already parsing!");
		}
		isParsing = true;
		index = 0;
		line = 1;
		depth = 0;
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
			depth++;
			this.checkDepth();
			attributes = this.parseMap(content, '(', ')').getValueMap();
			depth--;
			index++;
			this.skipWhiteSpaces(content);
		}else {
			attributes = new HashMap<>();
		}
		BaseValue value = null;

		depth++;
		this.checkDepth();
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
			value = this.parseNumber(content);
		}else if(c == 't' || c == 'f') {
			value = this.parseBoolean(content);
		}else if(Character.toLowerCase(c) == 'n') {
			value = this.parseNull(content);
		}else {
			throw new SyntaxException(line, "Not expected '" + c + "'");
		}
		depth--;
		value.setAttributes(attributes);
		return value;
	}

	private NullValue parseNull(char[] content) {
		assertEOF(content);
		if(Character.toLowerCase(content[index]) != 'n') {
			throw new SyntaxException(line, "Expected null token!");
		}
		index++;
		assertEOF(content);
		if(Character.toLowerCase(content[index]) != 'u') {
			throw new SyntaxException(line, "Expected null token!");
		}
		index++;
		assertEOF(content);
		if(Character.toLowerCase(content[index]) != 'l') {
			throw new SyntaxException(line, "Expected null token!");
		}
		index++;
		assertEOF(content);
		if(Character.toLowerCase(content[index]) != 'l') {
			throw new SyntaxException(line, "Expected null token!");
		}
		return new NullValue();
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
		final Map<String, BaseValue> map = new HashMap<>();
		String fieldName = null;
		boolean parsingField = false;
		while(true) {
			assertEOF(content);
			this.skipWhiteSpaces(content);
			char c = content[index];

			if(c == endChar) {
				break;
			}

			if(!parsingField && (c == ',' || c == ';')) {
				index++;
				continue;
			}

			if(!parsingField && ConfigUtils.isValidMapKeyCharacter(c)) {
				parsingField = true;
				fieldName = c == '"' ? this.parseString(content).getValue() : this.parseFieldName(content);
				continue;
			}

			if(parsingField && (c == '=' || c == ':')) {
				this.skipWhiteSpaces(content);
				if(map.containsKey(fieldName)) {
					throw new SyntaxException(line, "Duplicate field \"" + fieldName + "\".");
				}
				index++;
				map.put(fieldName, this.parse0(content));
				index++;
				parsingField = false;
				continue;
			}
			
			index++;
		}
		return new MapValue(map);
	}

	private String parseFieldName(char[] content) {
		skipWhiteSpaces(content);
		int fieldNameStart = index;
		String fieldName = null;
		while (true) {
			assertEOF(content);
			if (!ConfigUtils.isValidMapKeyCharacter(content[index])) {
				fieldName = new String(Arrays.copyOfRange(content, fieldNameStart, index));
				break;
			}
			index++;
		}
		return fieldName;
	}
	
	private NumberValue parseNumber(char[] content) {
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
			}else if(c == 'E' && hasE) {
				throw new SyntaxException(line, "Unexpected character in number: " + c);
			}
			if(c != '.' && c != '-' && !ConfigUtils.isDigit(c)) {
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
		return new BooleanValue(Boolean.parseBoolean(result));
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

			if(escaping) {
				escaping = false;
			}
			
			index++;
		}
		final String str = ConfigUtils.unescapeString(new String(Arrays.copyOfRange(content, start, index)));
		if(str.length() == 0) {
			throw new SyntaxException(line, "Character value can't be empty!");
		}else if(str.length() > 1) {
			throw new SyntaxException(line, "Character value can't have more than one character!");
		}
		return new CharValue(str.charAt(0));
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
		if (index >= content.length) {
			throw new SyntaxException(line, "Not expected '<eof>'");
		}
	}

	private void checkDepth() {
		if(maxDepth < 1) {
			return;
		}
		if(depth > maxDepth) {
			final SyntaxException ex = new SyntaxException(line, "Depth check failed: depth > " + maxDepth);
			ex.setStackTrace(Arrays.stream(ex.getStackTrace()).filter(ste -> !ste.getClassName().equals(this.getClass().getName())).toArray(StackTraceElement[]::new));
			throw ex;
		}
	}

	public BaseValue loadFile(File file) throws IOException {
		final BufferedReader br = new BufferedReader(new FileReader(file));
		final StringBuilder sb = new StringBuilder();
		String line = null;
		while((line = br.readLine()) != null) {
			sb.append(line).append(System.lineSeparator());
		}
		br.close();
		return this.parse(sb.toString());
	}

	public int getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}
}
