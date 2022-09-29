package codek;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class Program {
	private static final byte[] byteSignature = { Byte.parseByte("41", 16), Byte.parseByte("42", 16),
			Byte.parseByte("43", 16), Byte.parseByte("44", 16) };
	private static final int version = 1;
	private static final int contextCompression = 0;
	private static final int noContextCompression = 0;
	private static final int interferenceProtection = 0;
//	private static final byte[] maxSize = { 0, 0, 0, (byte) 42, (byte) 94, (byte) 96, (byte) 72, (byte) 96 };
	private static final int nameSize = 16;

	private static final int headersSize = byteSignature.length + 4 + nameSize;

	public static byte[] pack(String filePath) {

		String[] str = filePath.split("/");
		String simpleFileName = str[str.length - 1];

		byte[] nameBytes = simpleFileName.getBytes();
		if (nameBytes.length > nameSize) {
			System.out.println("too large filename");
			return null;
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			FileInputStream fileInputStream = new FileInputStream(filePath);
			byte[] fileBytes = fileInputStream.readAllBytes();
			fileInputStream.close();
			outputStream.write(byteSignature);
			outputStream.write((byte) version);
			outputStream.write((byte) contextCompression);
			outputStream.write((byte) noContextCompression);
			outputStream.write((byte) interferenceProtection);
			for (int i = 0; i < nameSize - nameBytes.length; i++) {
				outputStream.write((byte) 0);
			}
			outputStream.write(nameBytes);
			outputStream.write(fileBytes);
			return outputStream.toByteArray();
		} catch (IOException e) {
			System.out.println("packing error");
		}
		return null;
	}

	public static Map.Entry<String, byte[]> unpack(String filePath) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			FileInputStream fileInputStream = new FileInputStream(filePath);
			byte[] fileBytes = fileInputStream.readAllBytes();
			fileInputStream.close();
			for (int i = headersSize; i < fileBytes.length; i++) {
				outputStream.write(fileBytes[i]);
			}
			byte[] nameBytes = new byte[nameSize];
			for (int i = headersSize - nameSize; i < headersSize; i++) {
				nameBytes[i - (headersSize - nameSize)] = fileBytes[i];
			}
			int zeros = 0;
			while(nameBytes[zeros] == 0) {
				zeros += 1;
			}
			byte[] name = new byte[nameSize - zeros];
			for(int i = 0; i < nameSize - zeros; i++) {
				name[i] = nameBytes[i + zeros];
			}
			String fileName = new String(name);
			return Map.entry(fileName.trim(), outputStream.toByteArray());
		} catch (IOException e) {
			System.out.println("unpacking error");
		}
		return null;
	}

	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("wrong arguments count");
			return;
		}
		if (args[0].equals("pack")) {
			byte[] bytes = pack(args[1]);
			try {
				FileOutputStream outputStream = new FileOutputStream(args[2]);
				outputStream.write(bytes);
				outputStream.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (args[0].equals("unpack")) {
			Map.Entry<String, byte[]> fileMap = unpack(args[1]);
			try {
				File file = new File(args[2]);
				FileOutputStream outputStream = new FileOutputStream(path);
				outputStream.write(fileMap.getValue());
				outputStream.close();
			} catch (FileNotFoundException e) {
				System.out.println("file not found");
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
