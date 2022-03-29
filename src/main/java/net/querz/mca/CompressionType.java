package net.querz.mca;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

public enum CompressionType {

	NONE(0, new ExceptionFunction<OutputStream, OutputStream, IOException>() {
			public OutputStream accept(OutputStream s) { return s; }
		}, new ExceptionFunction<InputStream, InputStream, IOException>() {
			public InputStream accept(InputStream s) { return s; }
		}),
	GZIP(1, new ExceptionFunction<OutputStream, GZIPOutputStream, IOException>() {
			public GZIPOutputStream accept(OutputStream s) throws IOException {
				return new GZIPOutputStream(s);
			}
		}, new ExceptionFunction<InputStream, GZIPInputStream, IOException>() {
			public GZIPInputStream accept(InputStream s) throws IOException {
				return new GZIPInputStream(s);
			}
		}),
	ZLIB(2, new ExceptionFunction<OutputStream, DeflaterOutputStream, IOException>() {
			public DeflaterOutputStream accept(OutputStream s) throws IOException {
				return new DeflaterOutputStream(s);
			}
		}, new ExceptionFunction<InputStream, InflaterInputStream, IOException>() {
			public InflaterInputStream accept(InputStream s) throws IOException {
				return new InflaterInputStream(s);
			}
		});

	private byte id;
	private ExceptionFunction<OutputStream, ? extends OutputStream, IOException> compressor;
	private ExceptionFunction<InputStream, ? extends InputStream, IOException> decompressor;

	CompressionType(int id,
					ExceptionFunction<OutputStream, ? extends OutputStream, IOException> compressor,
					ExceptionFunction<InputStream, ? extends InputStream, IOException> decompressor) {
		this.id = (byte) id;
		this.compressor = compressor;
		this.decompressor = decompressor;
	}

	public byte getID() {
		return id;
	}

	public OutputStream compress(OutputStream out) throws IOException {
		return compressor.accept(out);
	}

	public InputStream decompress(InputStream in) throws IOException {
		return decompressor.accept(in);
	}

	public static CompressionType getFromID(byte id) {
		for (CompressionType c : CompressionType.values()) {
			if (c.id == id) {
				return c;
			}
		}
		return null;
	}
}
