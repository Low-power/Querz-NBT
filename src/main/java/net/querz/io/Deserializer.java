package net.querz.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public abstract class Deserializer<T> {

	public abstract T fromStream(InputStream stream) throws IOException;

	public T fromFile(File file) throws IOException {
		try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
			return fromStream(bis);
		}
	}

	public T fromBytes(byte[] data) throws IOException {
		ByteArrayInputStream stream = new ByteArrayInputStream(data);
		return fromStream(stream);
	}

	public T fromResource(Class<?> clazz, String path) throws IOException {
		try (InputStream stream = clazz.getClassLoader().getResourceAsStream(path)) {
			if (stream == null) {
				throw new IOException("resource \"" + path + "\" not found");
			}
			return fromStream(stream);
		}
	}

	public T fromURL(URL url) throws IOException {
		try (InputStream stream = url.openStream()) {
			return fromStream(stream);
		}
	}

}
