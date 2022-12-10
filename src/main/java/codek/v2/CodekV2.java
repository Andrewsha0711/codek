package codek.v2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import codek.Codek;
import codek.algorithm.Algorithm;
import codek.algorithm.Output;
import codek.algorithm.Rle;
import codek.algorithm.ShannonAlgorithm;

public class CodekV2 implements Codek {
	Algorithm shannonAlgorithm = new ShannonAlgorithm();
	Algorithm rle = new Rle();
	@Override
	public ByteArrayOutputStream pack(String absolutePath, String root,
			ByteArrayOutputStream outputStream, boolean headers) {
		try {
			if (headers) {
				outputStream.write(Signature.byteSignature);
				outputStream.write((byte) Signature.version);
			}
			return packFile(absolutePath, root, outputStream);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ByteArrayOutputStream packFile(String absolutePath, String root,
			ByteArrayOutputStream outputStream) {

		File file = new File(absolutePath);
		// if directory then go inside
		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			for (int i = 0; i < childFiles.length; i++) {
				String absPath = childFiles[i].getAbsolutePath();
				String childRoot = absPath.substring(absPath.indexOf(root));
				packFile(childFiles[i].getAbsolutePath(), childRoot, outputStream);
			}
			return outputStream;
		}
		// if file then pack
		byte[] nameBytes = root.getBytes();
		try {
			FileInputStream fileInputStream = new FileInputStream(absolutePath);
			// algorithm
			// Output algOut = this.shannonAlgorithm.algorithm(fileInputStream);
			Output algOut = this.rle.algorithm(fileInputStream);

			outputStream.write((byte) Signature.contextCompression);
			// algorithm code
			if (algOut.profit > 0) {
				outputStream.write((byte) 1);
			} else {
				outputStream.write((byte) 0);
			}
			outputStream.write((byte) Signature.interferenceProtection);
			// write size of content as bytes
			byte[] sizeBytes =
					ByteBuffer.allocate(Signature.size).putInt(algOut.contentSize).array();
			outputStream.write(sizeBytes);
			outputStream.write((byte) nameBytes.length);
			outputStream.write(nameBytes);
			// write output
			algOut.outputStream.writeTo(outputStream);
			algOut.outputStream.close();
			return outputStream;
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
		try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
			// siganture check
			byte signature[] = fileInputStream.readNBytes(Signature.byteSignature.length);
			for (int i = 0; i < Signature.byteSignature.length; i++) {
				if (!(signature[i] == Signature.byteSignature[i])) {
					throw new IllegalStateException("wrong file signature");
				}
			}
			int version = fileInputStream.read();
			int readed;
			while ((readed = fileInputStream.read()) != -1) {
				int contextCompression = readed;
				int noContextCompression = fileInputStream.read();
				int interferenceProtection = fileInputStream.read();

				byte[] currentFileSize = fileInputStream.readNBytes(Signature.size);
				int nameSize = fileInputStream.read();
				byte[] currentFileName = fileInputStream.readNBytes(nameSize);
				String tmp = new String(currentFileName);
				String fileName = target.concat((tmp).strip());
				File file = new File(fileName);
				file.getParentFile().mkdirs();
				// write current file content
				int contentSize = ByteBuffer.wrap(currentFileSize).getInt();
				if (file.createNewFile()) {
					FileOutputStream fileOutputStream =
							new FileOutputStream(file.getAbsolutePath());
					if (noContextCompression == 1) {
						Output output = this.rle.parse(fileInputStream, contentSize);
						output.outputStream.writeTo(fileOutputStream);
						output.outputStream.close();
					} else {
						fileOutputStream.write(fileInputStream.readNBytes(contentSize));
					}
					fileOutputStream.close();
				}
			}
			fileInputStream.close();
		} catch (IOException e) {
			System.out.println("unpacking error");
		}
	}
}
