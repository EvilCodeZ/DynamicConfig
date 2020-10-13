package de.evilcodez.config.serialization;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

public class BinaryConfig {

	private static final Map<Class<? extends BaseValue>, Integer> CLASS_TO_IDMAP = new HashMap<>();
	private static final Map<Integer, Class<? extends BaseValue>> ID_TO_CLASSMAP = new HashMap<>();
	private static final byte[] MAGIC_BYTES = "DCONF".getBytes(StandardCharsets.US_ASCII);
	public static final int USE_MAGIC = 1 << 1;

	public static void writeValue(BaseValue value, int flags, OutputStream output) throws IOException {
		DataOutputStream out = null;
		boolean useMagic = (flags & USE_MAGIC) != 0;
		if (useMagic) {
			output.write(MAGIC_BYTES);
		}
		out = new DataOutputStream(output);
		writeValue0(value, out);
	}

	private static void writeValue0(BaseValue value, DataOutputStream out) throws IOException {
		int id = CLASS_TO_IDMAP.getOrDefault(value.getClass(), -1);
		if (id == -1) {
			throw new IllegalArgumentException("Unknown BaseValue: " + value.getClass().getName());
		}
		if (value instanceof StringValue) {
			out.writeByte(id);
			writeMap(value.getAttributes(), out);
			writeString(((StringValue) value).getValue(), out);
		} else if (value instanceof NumberValue) {
			writeNumber((NumberValue) value, id, out);
		} else if (value instanceof CharValue) {
			out.writeByte(id);
			writeMap(value.getAttributes(), out);
			out.writeShort(((CharValue) value).getValue());
		} else if (value instanceof ListValue) {
			final List<BaseValue> list = ((ListValue) value).getElements();
			out.writeByte(id);
			writeMap(value.getAttributes(), out);
			out.writeInt(list.size());
			for (int i = 0; i < list.size(); i++) {
				writeValue0(list.get(i), out);
			}
		}else if(value instanceof BooleanValue) {
			if(((BooleanValue) value).getValue()) {
				out.writeByte((1 << 4) | id);
			}else {
				out.writeByte(id);
			}
		}else if (value instanceof MapValue) {
			final Map<String, BaseValue> map = ((MapValue) value).getValueMap();
			out.writeByte(id);
			writeMap(value.getAttributes(), out);
			writeMap(map, out);
		}
	}
	
	private static void writeMap(Map<String, BaseValue> map, DataOutputStream out) throws IOException {
		out.writeInt(map.size());
		for (String key : map.keySet()) {
			writeString(key, out);
			writeValue0(map.get(key), out);
		}
	}

	private static void writeNumber(NumberValue value, int id, DataOutputStream out) throws IOException {
		final Number num = value.getValue();
		if (num instanceof Float) {
			out.writeByte((9 << 4) | id);
			writeMap(value.getAttributes(), out);
			out.writeFloat(num.floatValue());
		} else if (num instanceof Double) {
			double val = num.doubleValue();
			if(val >= Float.MIN_VALUE && val <= Float.MAX_VALUE) {
				out.writeByte((9 << 4) | id);
				writeMap(value.getAttributes(), out);
				out.writeFloat(num.floatValue());
			}else {
				out.writeByte((10 << 4) | id);
				writeMap(value.getAttributes(), out);
				out.writeDouble(num.doubleValue());
			}
		}else {
			long val = num.longValue();
			if (val >= Byte.MIN_VALUE && val <= Byte.MAX_VALUE) {
				out.writeByte((1 << 4) | id);
				writeMap(value.getAttributes(), out);
				out.writeByte(num.byteValue());
			} else if (val >= Short.MIN_VALUE && val <= Short.MAX_VALUE) {
				out.writeByte((2 << 4) | id);
				writeMap(value.getAttributes(), out);
				out.writeShort(num.shortValue());
			} else if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
				out.writeByte((4 << 4) | id);
				writeMap(value.getAttributes(), out);
				out.writeInt(num.intValue());
			} else {
				out.writeByte((8 << 4) | id);
				writeMap(value.getAttributes(), out);
				out.writeLong(num.longValue());
			}
		}
	}

	private static void writeString(String value, DataOutputStream out) throws IOException {
		final byte[] data = value.getBytes(StandardCharsets.UTF_8);
		out.writeInt(data.length);
		out.write(data);
	}

	public static BaseValue readValue(int flags, InputStream input) throws IOException {
		return readValue(flags, input, 512);
	}

	public static BaseValue readValue(int flags, InputStream input, int maxDepth) throws IOException {
		DataInputStream in = null;
		boolean useMagic = (flags & USE_MAGIC) != 0;
		if (useMagic) {
			byte[] magicBytes = new byte[MAGIC_BYTES.length];
			input.read(magicBytes);
			if (!Arrays.equals(magicBytes, MAGIC_BYTES)) {
				throw new IOException("Magic verification failed!");
			}
		}
		in = new DataInputStream(input);
		return readValue0(in, 0, maxDepth);
	}

	private static BaseValue readValue0(DataInputStream in, int depth, int maxDepth) throws IOException {

		if(maxDepth > 0 && depth > maxDepth) {
			final IOException ex = new IOException("Depth check failed: depth > " + maxDepth);
			ex.setStackTrace(Arrays.stream(ex.getStackTrace()).filter(ste -> !ste.getClassName().equals(BinaryConfig.class.getName())).toArray(StackTraceElement[]::new));
			throw ex;
		}

		int bitmask = in.readUnsignedByte();
		int id = bitmask & 0xF;
		int additionalData = bitmask >> 4;
		
		Class<? extends BaseValue> clazz = ID_TO_CLASSMAP.get(id);
		if(clazz == null) {
			throw new IOException("Bad value id: " + id);
		}
		
		int count = in.readInt();
		final Map<String, BaseValue> attribs = new HashMap<>(count);
		for(int i = 0; i < count; i++) {
			attribs.put(readString(in), readValue0(in, depth + 1, maxDepth));
		}
		BaseValue value = null;
		
		if(clazz == StringValue.class) {
			value = new StringValue(readString(in));
		}else if(clazz == CharValue.class) {
			value = new CharValue(in.readChar());
		}else if(clazz == NumberValue.class) {
			value = new NumberValue(readNumber(in, additionalData));
		}else if(clazz == ListValue.class) {
			count = in.readInt();
			final List<BaseValue> list = new ArrayList<BaseValue>(count);
			for(int i = 0; i < count; i++) {
				list.add(readValue0(in, depth + 1, maxDepth));
			}
			value = new ListValue(list);
		}else if(clazz == MapValue.class) {
			count = in.readInt();
			final Map<String, BaseValue> map = new HashMap<>(count);
			for(int i = 0; i < count; i++) {
				map.put(readString(in), readValue0(in, depth + 1, maxDepth));
			}
			value = new MapValue(map);
		}else if(clazz == BooleanValue.class) {
			value = new BooleanValue(additionalData > 0);
		}
		value.setAttributes(attribs);
		return value;
	}
	
	private static Number readNumber(DataInputStream in, int type) throws IOException {
		if(type == 1) {
			return new Byte(in.readByte());
		}else if(type == 2) {
			return new Short(in.readShort());
		}else if(type == 4) {
			return new Integer(in.readInt());
		}else if(type == 8) {
			return new Long(in.readLong());
		}else if(type == 9) {
			return new Float(in.readFloat());
		}else if(type == 10) {
			return new Double(in.readDouble());
		}else {
			throw new IOException("Unknown number type: " + type);
		}
	}
	
	private static String readString(DataInputStream in) throws IOException {
		byte[] data = new byte[in.readInt()];
		in.readFully(data);
		return new String(data, StandardCharsets.UTF_8);
	}

	public static void saveFile(BaseValue value, File file) throws IOException {
		final FileOutputStream output = new FileOutputStream(file);
		BinaryConfig.writeValue(value, USE_MAGIC, output);
		output.flush();
		output.close();
	}

	public static BaseValue loadFile(File file) throws IOException {
		final FileInputStream input = new FileInputStream(file);
		final BaseValue value = BinaryConfig.readValue(USE_MAGIC, input);
		input.close();
		return value;
	}

	static {
		CLASS_TO_IDMAP.put(MapValue.class, 1);
		CLASS_TO_IDMAP.put(ListValue.class, 2);
		CLASS_TO_IDMAP.put(CharValue.class, 3);
		CLASS_TO_IDMAP.put(NumberValue.class, 4);
		CLASS_TO_IDMAP.put(StringValue.class, 5);
		CLASS_TO_IDMAP.put(BooleanValue.class, 6);
		CLASS_TO_IDMAP.forEach((k, v) -> ID_TO_CLASSMAP.put(v, k));
	}
}
