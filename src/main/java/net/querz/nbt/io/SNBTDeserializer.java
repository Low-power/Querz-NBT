package net.querz.nbt.io;

import net.querz.io.StringDeserializer;
import net.querz.nbt.tag.Tag;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class SNBTDeserializer extends StringDeserializer<Tag<?>> {

	@Override
	public Tag<?> fromReader(Reader reader) throws IOException {
		return fromReader(reader, Tag.DEFAULT_MAX_DEPTH);
	}

	public Tag<?> fromReader(Reader reader, int maxDepth) throws IOException {
		BufferedReader bufferedReader;
		if (reader instanceof BufferedReader) {
			bufferedReader = (BufferedReader) reader;
		} else {
			bufferedReader = new BufferedReader(reader);
		}
		StringBuilder s = new StringBuilder();
		String line;
		while((line = bufferedReader.readLine()) != null) s.append(line);
		return SNBTParser.parse(s.toString(), maxDepth);
	}
}
