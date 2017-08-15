/**
 * 
 */
package com.valencia.jutils.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.DataFormatException;

import com.valencia.jutils.compression.CompressionUtils;
import com.valencia.jutils.compression.CompressionUtils.CompressedData;

/**
 * @author Gabriel Valencia, gee4vee@me.com
 *
 */
public class ObjectUtils {
	
	public static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		try {
			oos.writeObject(obj);
		} finally {
			oos.close();
		}
		
		return baos.toByteArray();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T deserialize(byte[] objData, Class<T> objClass) throws ClassNotFoundException, IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(objData);
		ObjectInputStream ois = new ObjectInputStream(bais);
		try {
			Object obj = ois.readObject();
			return (T)obj;
		} finally {
			ois.close();
		}
	}

	/**
	 * @param args
	 * @throws DataFormatException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws DataFormatException, IOException, ClassNotFoundException {
		String input = "blahblahblah";
		System.out.println("Input = " + input);
		System.out.println("Original length = " + input.length());
		System.out.println("Compressing...");
		CompressedData cdata = CompressionUtils.compressString(input);
		System.out.println("Compressed length = " + cdata.compressedLength);
		System.out.println("Decompressing...");
		byte[] ddata = CompressionUtils.decompressData(cdata);
		System.out.println("Decompressed length = " + ddata.length);
		System.out.println("Decompressed data = " + new String(ddata, 0, ddata.length, "UTF-8"));
		
		System.out.println("");
		System.out.println("Serializing compressed data...");
		byte[] sdata = serialize(cdata);
		CompressedData dsdata = deserialize(sdata, CompressedData.class);
		System.out.println("Decompressing deserialized data...");
		ddata = CompressionUtils.decompressData(dsdata);
		System.out.println("Decompressed length = " + ddata.length);
		System.out.println("Decompressed data = " + new String(ddata, 0, ddata.length, "UTF-8"));
	}

}
