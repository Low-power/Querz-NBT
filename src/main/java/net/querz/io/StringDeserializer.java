package net.querz.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

public abstract class StringDeserializer<T> extends Deserializer<T> {

	public abstract T fromReader(Reader reader) throws IOException;

	public T fromString(String s) throws IOException {
		return fromReader(new StringReader(s));
	}

	@Override
	public T fromStream(InputStream stream) throws IOException {
		try (Reader reader = new InputStreamReader(stream)) {
			return fromReader(reader);
		}
	}

	@Override
	public T fromFile(File file) throws IOException {
		try (Reader reader = new FileReader(file)) {
			return fromReader(reader);
		}
	}

	@Override
	public T fromBytes(byte[] data) throws IOException {
		return fromReader(new StringReader(new String(data)));
	}
}
