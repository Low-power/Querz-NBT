package net.querz.nbt.tag;

import java.util.Arrays;
import java.io.DataOutputStream;
import java.io.IOException;

public class IntArrayTag extends ArrayTag<int[]> implements Comparable<IntArrayTag> {

	public static final byte ID = 11;
	public static final int[] ZERO_VALUE = new int[0];

	public IntArrayTag() {
		super(ZERO_VALUE);
	}

	public IntArrayTag(int[] value) {
		super(value);
	}

	@Override
	public byte getID() {
		return ID;
	}

	@Override
	public boolean equals(Object other) {
		return super.equals(other) && Arrays.equals(getValue(), ((IntArrayTag) other).getValue());
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(getValue());
	}

	@Override
	public int compareTo(IntArrayTag other) {
		return Integer.compare(length(), other.length());
	}

	@Override
	public IntArrayTag clone() {
		return new IntArrayTag(Arrays.copyOf(getValue(), length()));
	}

	@Override
	public void write(DataOutputStream stream, int max_depth) throws IOException {
		stream.writeInt(length());
		for(int i : getValue()) {
			stream.writeInt(i);
		}
	}
}
