package com.genuinevm.data.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.genuinevm.data.Data;
import com.genuinevm.data.TypeSystem;
import com.genuinevm.data.collection.DataCompound;
import com.genuinevm.data.primitive.DataNull;

public class InputOutput {

	public static DataCompound open(final File file) throws IOException {
		if (!file.exists())
			return null;
		else {
			final DataInputStream input = new DataInputStream(new FileInputStream(file));
			DataCompound compound;
			try {
				compound = getDataCompound(input);
			}
			finally {
				input.close();
			}
			return compound;
		}
	}

	public static void save(final DataCompound compound, final File file) throws IOException {
		final DataOutputStream stream = new DataOutputStream(new FileOutputStream(file));
		try {
			writeToOutput(compound, stream);
		}
		finally {
			stream.close();
		}
	}

	public static void safeSave(final DataCompound compound, final File file) {
		try {
			final File tmp = new File(file.getAbsolutePath() + "_tmp");
			if (tmp.exists())
				tmp.delete();
			save(compound, tmp);
			if (file.exists())
				file.delete();
			if (file.exists())
				throw new IOException("Failed to delete " + file);
			else
				tmp.renameTo(file);
		}
		catch (final Exception e) {}
	}

	public static DataCompound readCompressed(final InputStream stream) throws IOException {
		final DataInputStream compressed = new DataInputStream(new BufferedInputStream(new GZIPInputStream(stream)));
		DataCompound data;
		try {
			data = getDataCompound(compressed);
		}
		finally {
			compressed.close();
		}
		return data;
	}

	public static DataCompound readCompressed(final byte[] bytes) throws IOException {
		final DataInputStream compressed = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes))));
		DataCompound compound;
		try {
			compound = getDataCompound(compressed);
		}
		finally {
			compressed.close();
		}
		return compound;
	}

	public static DataCompound getDataCompound(final DataInput input) throws IOException {
		final Data data = getData(input);
		if (data instanceof DataCompound)
			return (DataCompound) data;
		throw new IOException("DataCompound was not the containing data type");
	}

	private static Data getData(final DataInput input) throws IOException {
		final byte type = input.readByte();
		if (type == 0)
			return DataNull.INSTANCE;
		input.readUTF();
		final Data data = TypeSystem.getTypeSystem().createByCode(type);
		data.read(input);
		return data;
	}

	public static byte[] getBytes(final DataCompound compound) throws IOException {
		final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		final DataOutputStream compressedOutput = new DataOutputStream(byteArray);
		try {
			writeToOutput(compound, compressedOutput);
		}
		finally {
			compressedOutput.close();
		}
		return byteArray.toByteArray();
	}

	public static byte[] getCompressedBytes(final DataCompound compound) throws IOException {
		final ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		final DataOutputStream compressed = new DataOutputStream(new GZIPOutputStream(byteArray));
		try {
			writeToOutput(compound, compressed);
		}
		finally {
			compressed.close();
		}
		return byteArray.toByteArray();
	}

	public static void writeToCompressedStream(final DataCompound compound, final OutputStream stream) throws IOException {
		final DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(stream)));
		try {
			writeToOutput(compound, output);
		}
		finally {
			output.close();
		}
	}

	private static void writeToOutput(final Data data, final DataOutput output) throws IOException {
		output.writeByte(data.code());
		if (data.code() != 0) {
			output.writeUTF("");
			data.write(output);
		}
	}
}
