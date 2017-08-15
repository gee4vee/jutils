/**
 * 
 */
package com.valencia.jutils.compression;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Utilities for compressing/decompressing data.
 * 
 * @author Gabriel Valencia, gee4vee@me.com
 */
public class CompressionUtils {
	
	public static CompressedData compressData(byte[] data) {
		return compressData(data, false);
	}
	
	public static CompressedData compressData(byte[] data, boolean trimResult) {
		// sometimes compression can increase the # of bytes for certain combinations of small strings.
		byte[] output = new byte[data.length*2];
		Deflater comp = new Deflater(Deflater.BEST_COMPRESSION);
		comp.setInput(data);
		comp.finish();
		int cdataLen = comp.deflate(output);
		comp.end();
		if (trimResult) {
			output = Arrays.copyOf(output, cdataLen);
		}
		CompressedData cdata = new CompressedData(output, cdataLen, data.length);
		return cdata;
	}
	
	public static byte[] decompressData(CompressedData cdata) throws DataFormatException {
		byte[] result = new byte[cdata.originalLength];
		Inflater decomp = new Inflater();
		decomp.setInput(cdata.data);
		decomp.inflate(result);
		return result;
	}
	
	public static class CompressedData implements Serializable {
		private static final long serialVersionUID = 1L;
		
		final byte[] data;
		final int compressedLength;
		final int originalLength;
		
		public CompressedData(byte[] data, int compressedLength, int originalLength) {
			this.data = data;
			this.compressedLength = compressedLength;
			this.originalLength = originalLength;
		}
	}
	
	public static CompressedData compressString(String s) {
		byte[] data = s.getBytes(StandardCharsets.UTF_8);
		return compressData(data);
	}

	/**
	 * @param args
	 * @throws DataFormatException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws DataFormatException, UnsupportedEncodingException {
		String input = "blahblahblah";
		System.out.println("Input = " + input);
		System.out.println("Original length = " + input.length());
		System.out.println("Compressing...");
		CompressedData cdata = compressString(input);
		System.out.println("Compressed length = " + cdata.compressedLength);
		System.out.println("Decompressing...");
		byte[] ddata = decompressData(cdata);
		System.out.println("Decompressed length = " + ddata.length);
		System.out.println("Decompressed data = " + new String(ddata, 0, ddata.length, "UTF-8"));
		
		System.out.println("");
		input = "Hello world";
		System.out.println("Input = " + input);
		System.out.println("Original length = " + input.length());
		System.out.println("Compressing...");
		cdata = compressString(input);
		System.out.println("Compressed length = " + cdata.compressedLength);
		System.out.println("Decompressing...");
		ddata = decompressData(cdata);
		System.out.println("Decompressed length = " + ddata.length);
		System.out.println("Decompressed data = " + new String(ddata, 0, ddata.length, StandardCharsets.UTF_8));
		
		System.out.println("");
		input = "blahblahblahbl";
		System.out.println("Input = " + input);
		System.out.println("Original length = " + input.length());
		System.out.println("Compressing...");
		cdata = compressString(input);
		System.out.println("Compressed length = " + cdata.compressedLength);
		System.out.println("Decompressing...");
		ddata = decompressData(cdata);
		System.out.println("Decompressed length = " + ddata.length);
		System.out.println("Decompressed data = " + new String(ddata, 0, ddata.length, StandardCharsets.UTF_8));
		
		System.out.println("");
		input = "blahblahblahbla";
		System.out.println("Input = " + input);
		System.out.println("Original length = " + input.length());
		System.out.println("Compressing...");
		cdata = compressString(input);
		System.out.println("Compressed length = " + cdata.compressedLength);
		System.out.println("Decompressing...");
		ddata = decompressData(cdata);
		System.out.println("Decompressed length = " + ddata.length);
		System.out.println("Decompressed data = " + new String(ddata, 0, ddata.length, StandardCharsets.UTF_8));
		
		System.out.println("");
		input = "a;eofmksl;p124";
		System.out.println("Input = " + input);
		System.out.println("Original length = " + input.length());
		System.out.println("Compressing...");
		cdata = compressString(input);
		System.out.println("Compressed length = " + cdata.compressedLength);
		System.out.println("Decompressing...");
		ddata = decompressData(cdata);
		System.out.println("Decompressed length = " + ddata.length);
		System.out.println("Decompressed data = " + new String(ddata, 0, ddata.length, StandardCharsets.UTF_8));
		
		System.out.println("");
		input = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed rutrum imperdiet consequat. Nulla eu sapien tincidunt, pellentesque ipsum in, luctus eros. Nullam tristique arcu lorem, at fringilla lectus tincidunt sit amet. Ut tortor dui, cursus at erat non, interdum imperdiet odio. In hac habitasse platea dictumst. Nulla facilisi. Duis eget auctor nibh. Cras ante odio, dignissim et sem id, ultrices imperdiet erat. Aenean ut purus hendrerit, bibendum massa non, accumsan orci. Morbi quis leo sed mauris scelerisque vulputate. Fusce gravida facilisis ipsum pellentesque euismod. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae";
		System.out.println("Input = " + input);
		System.out.println("Original length = " + input.length());
		System.out.println("Compressing...");
		cdata = compressString(input);
		System.out.println("Compressed length = " + cdata.compressedLength);
		System.out.println("Decompressing...");
		ddata = decompressData(cdata);
		System.out.println("Decompressed length = " + ddata.length);
		System.out.println("Decompressed data = " + new String(ddata, 0, ddata.length, StandardCharsets.UTF_8));
	}

}
