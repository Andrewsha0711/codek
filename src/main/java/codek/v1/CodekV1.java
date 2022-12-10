package codek.v1;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import codek.Codek;

public class CodekV1 implements Codek {
	@Override
	public ByteArrayOutputStream pack(String absolutePath, String root,
			ByteArrayOutputStream outputStream, boolean headers) {
		try {
			outputStream.write(Signature.byteSignature);
			outputStream.write((byte) Signature.version);
			return packFile(absolutePath, root, outputStream);
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
	}

	public ByteArrayOutputStream packFile(String absolutePath, String root,
			ByteArrayOutputStream total) {

		File file = new File(absolutePath);
		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			for (int i = 0; i < childFiles.length; i++) {
				String absPath = childFiles[i].getAbsolutePath();
				String childRoot = absPath.substring(absPath.indexOf(root));
				this.packFile(childFiles[i].getAbsolutePath(), childRoot, total);
			}
			return total;
		}

		byte[] nameBytes = root.getBytes();
		if (nameBytes.length > Signature.nameSize) {
			System.out.println("too large filename");
			return null;
		}
		for (int i = 0; i < Signature.nameSize - nameBytes.length; i++) {
			root = ' ' + root;
		}
		nameBytes = root.getBytes();
		try {
			FileInputStream fileInputStream = new FileInputStream(absolutePath);
			byte[] fileBytes = fileInputStream.readAllBytes();
			fileInputStream.close();
			try {
				byte[] sizeBytes =
						ByteBuffer.allocate(Signature.size).putInt(fileBytes.length).array();
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

	@Override
	public void unpack(String filePath, String target) {
		if (!target.endsWith("/")) {
			target = target.concat("/");
		}
		try {
			FileInputStream fileInputStream = new FileInputStream(filePath);
			byte[] fileBytes = fileInputStream.readAllBytes();
			fileInputStream.close();
			// TODO check headers
			for (int i = 0; i < Signature.byteSignature.length; i++) {
				if (!(fileBytes[i] == Signature.byteSignature[i])) {
					throw new IllegalStateException("wrong file signature");
				}
			}

			int position = (Signature.headersSize - Signature.size - Signature.nameSize);

			while (position < fileBytes.length - 1) {
				// 1 current file size
				byte[] currentFileSize = new byte[Signature.size];
				for (int i = 0; i < Signature.size; i++) {
					currentFileSize[i] = fileBytes[position + i];
				}
				position += Signature.size;
				// 2 current file name
				byte[] currentFileName = new byte[Signature.nameSize];
				for (int i = 0; i < Signature.nameSize; i++) {
					currentFileName[i] = fileBytes[position + i];
				}
				position += Signature.nameSize;
				// 3 parse filename
				String fileName = target.concat((new String(currentFileName)).strip());
				File file = new File(fileName);
				file.getParentFile().mkdirs();
				// 4 write current file content
				int contentSize = ByteBuffer.wrap(currentFileSize).getInt();
				if (file.createNewFile()) {
					FileOutputStream fileOutputStream =
							new FileOutputStream(file.getAbsolutePath());
					fileOutputStream.write(fileBytes, position, contentSize);
				}
				position += contentSize;
			}

		} catch (IOException e) {
			System.out.println("unpacking error");
		}
	}


}
