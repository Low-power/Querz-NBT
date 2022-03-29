package net.querz.nbt.tag;

import net.querz.io.MaxDepthReachedException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Base class for all NBT tags.
 * 
 * <h1>Nesting</h1>
 * <p>All methods serializing instances or deserializing data track the nesting levels to prevent 
 * circular references or malicious data which could, when deserialized, result in thousands 
 * of instances causing a denial of service.</p>
 * 
 * <p>These methods have a parameter for the maximum nesting depth they are allowed to traverse. A 
 * value of {@code 0} means that only the object itself, but no nested objects may be processed. 
 * If an instance is nested further than allowed, a {@link MaxDepthReachedException} will be thrown.
 * Providing a negative maximum nesting depth will cause an {@code IllegalArgumentException} 
 * to be thrown.</p>
 * 
 * <p>Some methods do not provide a parameter to specify the maximum nesting depth, but instead use 
 * {@link #DEFAULT_MAX_DEPTH}, which is also the maximum used by Minecraft. This is documented for 
 * the respective methods.</p>
 * 
 * <p>If custom NBT tags contain objects other than NBT tags, which can be nested as well, then there 
 * is no guarantee that {@code MaxDepthReachedException}s are thrown for them. The respective class 
 * will document this behavior accordingly.</p>
 * 
 * @param <T> The type of the contained value
 * */
public abstract class Tag<T> implements Cloneable {

	/**
	 * The default maximum depth of the NBT structure.
	 * */
	public static final int DEFAULT_MAX_DEPTH = 512;

	private static final Map<String, String> ESCAPE_CHARACTERS;
	static {
		final Map<String, String> temp = new HashMap<>();
		temp.put("\\", "\\\\\\\\");
		temp.put("\n", "\\\\n");
		temp.put("\t", "\\\\t");
		temp.put("\r", "\\\\r");
		temp.put("\"", "\\\\\"");
		ESCAPE_CHARACTERS = Collections.unmodifiableMap(temp);
	}

	private static final Pattern ESCAPE_PATTERN = Pattern.compile("[\\\\\n\t\r\"]");
	private static final Pattern NON_QUOTE_PATTERN = Pattern.compile("[a-zA-Z0-9_\\-+]+");

	private T value;

	/**
	 * Initializes this Tag with some value. If the value is {@code null}, it will
	 * throw a {@code NullPointerException}
	 * @param value The value to be set for this Tag.
	 * */
	public Tag(T value) {
		setValue(value);
	}

	/**
	 * @return This Tag's ID, usually used for serialization and deserialization.
	 * */
	public abstract byte getID();

	/**
	 * @return The value of this Tag.
	 * */
	protected T getValue() {
		return value;
	}

	/**
	 * Sets the value for this Tag directly.
	 * @param value The value to be set.
	 * @throws NullPointerException If the value is null
	 * */
	protected void setValue(T value) {
		this.value = checkValue(value);
	}

	/**
	 * Checks if the value {@code value} is {@code null}.
	 * @param value The value to check
	 * @throws NullPointerException If {@code value} was {@code null}
	 * @return The parameter {@code value}
	 * */
	protected T checkValue(T value) {
		return Objects.requireNonNull(value);
	}

	/**
	 * Calls {@link Tag#toString(int)} with an initial depth of {@code 0}.
	 * @see Tag#toString(int)
	 * @throws MaxDepthReachedException If the maximum nesting depth is exceeded.
	 * */
	@Override
	public final String toString() {
		return toString(DEFAULT_MAX_DEPTH);
	}

	/**
	 * Creates a string representation of this Tag in a valid JSON format.
	 * @param maxDepth The maximum nesting depth.
	 * @return The string representation of this Tag.
	 * @throws MaxDepthReachedException If the maximum nesting depth is exceeded.
	 * */
	public String toString(int maxDepth) {
		return "{\"type\":\""+ getClass().getSimpleName() + "\"," +
				"\"value\":" + valueToString(maxDepth) + "}";
	}

	/**
	 * Calls {@link Tag#valueToString(int)} with {@link Tag#DEFAULT_MAX_DEPTH}.
	 * @return The string representation of the value of this Tag.
	 * @throws MaxDepthReachedException If the maximum nesting depth is exceeded.
	 * */
	public String valueToString() {
		return valueToString(DEFAULT_MAX_DEPTH);
	}

	/**
	 * Returns a JSON representation of the value of this Tag.
	 * @param maxDepth The maximum nesting depth.
	 * @return The string representation of the value of this Tag.
	 * @throws MaxDepthReachedException If the maximum nesting depth is exceeded.
	 * */
	public abstract String valueToString(int maxDepth);

	/**
	 * Returns whether this Tag and some other Tag are equal.
	 * They are equal if {@code other} is not {@code null} and they are of the same class.
	 * Custom Tag implementations should overwrite this but check the result
	 * of this {@code super}-method while comparing.
	 * @param other The Tag to compare to.
	 * @return {@code true} if they are equal based on the conditions mentioned above.
	 * */
	@Override
	public boolean equals(Object other) {
		return other != null && getClass() == other.getClass();
	}

	/**
	 * Calculates the hash code of this Tag. Tags which are equal according to {@link Tag#equals(Object)}
	 * must return an equal hash code.
	 * @return The hash code of this Tag.
	 * */
	@Override
	public int hashCode() {
		return value.hashCode();
	}

	/**
	 * Creates a clone of this Tag.
	 * @return A clone of this Tag.
	 * */
	@SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
	public abstract Tag<T> clone();

	/**
	 * Escapes a string to fit into a JSON-like string representation for Minecraft
	 * or to create the JSON string representation of a Tag returned from {@link Tag#toString()}
	 * @param s The string to be escaped.
	 * @param lenient {@code true} if it should force double quotes ({@code "}) at the start and
	 *                the end of the string.
	 * @return The escaped string.
	 * */
	protected static String escapeString(String s, boolean lenient) {
		StringBuffer sb = new StringBuffer();
		Matcher m = ESCAPE_PATTERN.matcher(s);
		while (m.find()) {
			m.appendReplacement(sb, ESCAPE_CHARACTERS.get(m.group()));
		}
		m.appendTail(sb);
		m = NON_QUOTE_PATTERN.matcher(s);
		if (!lenient || !m.matches()) {
			sb.insert(0, "\"").append("\"");
		}
		return sb.toString();
	}

	public abstract void write(DataOutputStream stream, int max_depth) throws IOException;

	private static Map<Class<?>, Byte> classIdMapping = new HashMap<>();
	private static Map<Byte, Class<?>> idClassMapping = new HashMap<>();
	static {
		classIdMapping.put(EndTag.class, Byte.valueOf(EndTag.ID));
		classIdMapping.put(ByteTag.class, Byte.valueOf(ByteTag.ID));
		classIdMapping.put(ShortTag.class, Byte.valueOf(ShortTag.ID));
		classIdMapping.put(IntTag.class, Byte.valueOf(IntTag.ID));
		classIdMapping.put(LongTag.class, Byte.valueOf(LongTag.ID));
		classIdMapping.put(FloatTag.class, Byte.valueOf(FloatTag.ID));
		classIdMapping.put(DoubleTag.class, Byte.valueOf(DoubleTag.ID));
		classIdMapping.put(StringTag.class, Byte.valueOf(StringTag.ID));
		classIdMapping.put(ListTag.class, Byte.valueOf(ListTag.ID));
		classIdMapping.put(CompoundTag.class, Byte.valueOf(CompoundTag.ID));
		classIdMapping.put(ByteArrayTag.class, Byte.valueOf(ByteArrayTag.ID));
		classIdMapping.put(IntArrayTag.class, Byte.valueOf(IntArrayTag.ID));
		classIdMapping.put(LongArrayTag.class, Byte.valueOf(LongArrayTag.ID));
		idClassMapping.put(Byte.valueOf(EndTag.ID), EndTag.class);
		idClassMapping.put(Byte.valueOf(ByteTag.ID), ByteTag.class);
		idClassMapping.put(Byte.valueOf(ShortTag.ID), ShortTag.class);
		idClassMapping.put(Byte.valueOf(IntTag.ID), IntTag.class);
		idClassMapping.put(Byte.valueOf(LongTag.ID), LongTag.class);
		idClassMapping.put(Byte.valueOf(FloatTag.ID), FloatTag.class);
		idClassMapping.put(Byte.valueOf(DoubleTag.ID), DoubleTag.class);
		idClassMapping.put(Byte.valueOf(StringTag.ID), StringTag.class);
		idClassMapping.put(Byte.valueOf(ListTag.ID), ListTag.class);
		idClassMapping.put(Byte.valueOf(CompoundTag.ID), CompoundTag.class);
		idClassMapping.put(Byte.valueOf(ByteArrayTag.ID), ByteArrayTag.class);
		idClassMapping.put(Byte.valueOf(IntArrayTag.ID), IntArrayTag.class);
		idClassMapping.put(Byte.valueOf(LongArrayTag.ID), LongArrayTag.class);
	}

	protected static int decrementMaxDepth(int max_depth) {
		if(max_depth < 0) throw new IllegalArgumentException("negative maximum depth is not allowed");
		if(max_depth == 0) throw new MaxDepthReachedException("reached maximum depth of NBT structure");
		return --max_depth;
	}

	protected static byte idFromClass(Class<?> clazz) {
		Byte id = classIdMapping.get(clazz);
		if (id == null) {
			throw new IllegalArgumentException("unknown Tag class " + clazz.getName());
		}
		return id.byteValue();
	}

	public static Tag<?> read(byte type_id, DataInputStream stream, int max_depth) throws IOException {
		switch(type_id) {
			case EndTag.ID:
				return EndTag.INSTANCE;
			case ByteTag.ID:
				return new ByteTag(stream.readByte());
			case ShortTag.ID:
				return new ShortTag(stream.readShort());
			case IntTag.ID:
				return new IntTag(stream.readInt());
			case LongTag.ID:
				return new LongTag(stream.readLong());
			case FloatTag.ID:
				return new FloatTag(stream.readFloat());
			case DoubleTag.ID:
				return new DoubleTag(stream.readDouble());
			case StringTag.ID:
				return new StringTag(stream.readUTF());
			case ListTag.ID:
				type_id = stream.readByte();
				{
					ListTag<?> list = ListTag.createUnchecked(idClassMapping.get(Byte.valueOf(type_id)));
					int length = stream.readInt();
					if(length < 0) length = 0;
					for (int i = 0; i < length; i++) {
						list.addUnchecked(read(type_id, stream, decrementMaxDepth(max_depth)));
					}
					return list;
				}
			case CompoundTag.ID:
				{
					CompoundTag comp = new CompoundTag();
					int id;
					while((id = stream.readByte() & 0xff) != 0) {
						String key = stream.readUTF();
						Tag<?> element = read((byte)id, stream, decrementMaxDepth(max_depth));
						comp.put(key, element);
					}
					return comp;
				}
			case ByteArrayTag.ID:
				{
					ByteArrayTag t = new ByteArrayTag(new byte[stream.readInt()]);
					stream.readFully(t.getValue());
					return t;
				}
			case IntArrayTag.ID:
				{
					int[] a = new int[stream.readInt()];
					IntArrayTag t = new IntArrayTag(a);
					for(int i = 0; i < a.length; i++) a[i] = stream.readInt();
					return t;
				}
			case LongArrayTag.ID:
				{
					long[] a = new long[stream.readInt()];
					LongArrayTag t = new LongArrayTag(a);
					for(int i = 0; i < a.length; i++) a[i] = stream.readLong();
					return t;
				}
			default:
				throw new IOException("invalid tag type id " + String.valueOf(type_id));
		}
	}

/*
	public static Tag<?> read(DataInputStream stream, int max_depth) throws IOException {
		byte type_id = stream.readByte();
		return read(type_id, stream, max_depth);
	}
*/
}
