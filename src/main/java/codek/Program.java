package codek;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class Program {
	private static final byte[] byteSignature = { Byte.parseByte("41", 16), Byte.parseByte("42", 16),
			Byte.parseByte("43", 16), Byte.parseByte("44", 16) };
	private static final int version = 1;
	private static final int contextCompression = 0;
	private static final int noContextCompression = 0;
	private static final int interferenceProtection = 0;
	private static final int size = 4;
	private static final int nameSize = 128;

	private static final int headersSize = byteSignature.length + 4 + size + nameSize;

	public static ByteArrayOutputStream pack(String absolutePath, String root, boolean flag,
			ByteArrayOutputStream total) {
		// write headers flag
		if (flag) {
			try {
				total.write(byteSignature);
			} catch (IOException e1) {
				System.out.println("packing error: failed to write signature");
				e1.printStackTrace();
			}
			total.write((byte) version);
			total.write((byte) contextCompression);
			total.write((byte) noContextCompression);
			total.write((byte) interferenceProtection);
		}

		File file = new File(absolutePath);
		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			for (int i = 0; i < childFiles.length; i++) {
				String absPath = childFiles[i].getAbsolutePath();
				String childRoot = absPath.substring(absPath.indexOf(root));
				pack(childFiles[i].getAbsolutePath(), childRoot, false, total);
			}
			return total;
		}

		byte[] nameBytes = root.getBytes();
		if (nameBytes.length > nameSize) {
			System.out.println("too large filename");
			return null;
		}
		for (int i = 0; i < nameSize - nameBytes.length; i++) {
			root = ' ' + root;
		}
		nameBytes = root.getBytes();
		try {
			FileInputStream fileInputStream = new FileInputStream(absolutePath);
			byte[] fileBytes = fileInputStream.readAllBytes();
			fileInputStream.close();
			try {
				byte[] sizeBytes = ByteBuffer.allocate(size).putInt(fileBytes.length).array();
				total.write(sizeBytes);
			} catch (BufferOverflowException e) {
				System.out.println("too large file: " + root);
				return null;
			}
			total.write(nameBytes);
			total.write(fileBytes);
			return total;
		} catch (IOException e) {
			System.out.println("packing error");
		}
		return null;
	}

	public static void unpack(String filePath, String target) {
		if (!target.endsWith("/")) {
			target = target.concat("/");
		}
		try {
			FileInputStream fileInputStream = new FileInputStream(filePath);
			byte[] fileBytes = fileInputStream.readAllBytes();
			fileInputStream.close();

			int position = 0;
			// TODO check headers
			position += (headersSize - size - nameSize);

			while (position < fileBytes.length - 1) {
				// 1 current file size
				byte[] currentFileSize = new byte[size];
				for (int i = 0; i < size; i++) {
					currentFileSize[i] = fileBytes[position + i];
				}
				position += size;
				// 2 current file name
				byte[] currentFileName = new byte[nameSize];
				for (int i = 0; i < nameSize; i++) {
					currentFileName[i] = fileBytes[position + i];
				}
				position += nameSize;
				// 3 parse filename
				String fileName = target.concat((new String(currentFileName)).strip());
				File file = new File(fileName);
				file.getParentFile().mkdirs();
				// 4 write current file content
				int contentSize = ByteBuffer.wrap(currentFileSize).getInt();
				if (file.createNewFile()) {
					FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsolutePath());
					fileOutputStream.write(fileBytes, position, contentSize);
				}
				position += contentSize;
			}

		} catch (IOException e) {
			System.out.println("unpacking error");
		}
	}

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("wrong arguments count");
			args = new String[6];
			// pack
//			args[0] = "pack";
//			args[1] = "/home/Andrewsha/workspace/temp/test/1.jpg";
//			args[2] = "/home/Andrewsha/workspace/temp/test/dallas.png";
//			args[3] = "/home/Andrewsha/workspace/temp/test/phone";
//			args[4] = "/home/Andrewsha/workspace/temp/test/test1";
//			args[5] = "/home/Andrewsha/workspace/temp/oleg.oleg";
			// unpack
//			args[0] = "unpack";
//			args[1] = "/home/Andrewsha/workspace/temp/oleg.oleg";
//			args[2] = "/home/Andrewsha/workspace/temp/test2";
//			return;
		}
		if (args[0].equals("pack")) {
			String target = args[args.length - 1];
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			boolean writeHeadersFlag = true;
			for (int i = 1; i < args.length - 1; i++) {
				String root = args[i].split("/")[args[i].split("/").length - 1];
				bytes = pack(args[i], root, writeHeadersFlag, bytes);
				writeHeadersFlag = false;
			}
			try {
				FileOutputStream outputStream = new FileOutputStream(target);
				bytes.writeTo(outputStream);
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
			unpack(args[1], args[2]);
		}
	}

}
