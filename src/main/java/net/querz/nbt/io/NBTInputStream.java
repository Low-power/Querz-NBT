package net.querz.nbt.io;

import net.querz.nbt.tag.ByteArrayTag;
import net.querz.nbt.tag.ByteTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.DoubleTag;
import net.querz.nbt.tag.EndTag;
import net.querz.nbt.tag.FloatTag;
import net.querz.nbt.tag.IntArrayTag;
import net.querz.nbt.tag.IntTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.LongArrayTag;
import net.querz.nbt.tag.LongTag;
import net.querz.nbt.tag.ShortTag;
import net.querz.nbt.tag.StringTag;
import net.querz.nbt.tag.Tag;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class NBTInputStream extends DataInputStream {
	public NBTInputStream(InputStream in) {
		super(in);
	}

	public NamedTag readTag(int maxDepth) throws IOException {
		byte id = readByte();
		return new NamedTag(readUTF(), Tag.read(id, this, maxDepth));
	}

	public Tag<?> readRawTag(int maxDepth) throws IOException {
		byte id = readByte();
		return Tag.read(id, this, maxDepth);
	}
}
