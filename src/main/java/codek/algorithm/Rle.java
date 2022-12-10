package codek.algorithm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Rle
 */
public class Rle implements Algorithm {
	private static final int minLength = 3;

	@Override
	public Output algorithm(InputStream inputStream) throws IOException {
		int readed = inputStream.read();
		if (readed == -1) {
			throw new IOException("cannot read first byte");
		}
		int c = readed;
		int l = 1;
		int contentSize = 0;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			while (readed != -1) {
				while ((readed = inputStream.read()) == c && l < 127) {
					l++;
				}
				if (l >= minLength) {
					// compressed bytes chain
					String binary = String.format("%8s", Integer.toBinaryString(l & 0xFF))
							.replace(' ', '0');
					binary = "1" + binary.substring(1);
					outputStream.write((byte) Integer.parseInt(binary, 2));
					outputStream.write((byte) c);
					contentSize += 2;
				} else {
					// uncompressed bytes chain
					List<Byte> bytes = new ArrayList<>();
					for (int i = 0; i < l; i++) {
						bytes.add(Byte.valueOf((byte) c));
					}
					while (l != minLength) {
						if (readed != -1) {
							bytes.add(Byte.valueOf((byte) readed));
							readed = inputStream.read();
							l++;
						} else {
							break;
						}
					}
					String binary =
							String.format("%8s", Integer.toBinaryString(bytes.size() & 0xFF))
									.replace(' ', '0');
					outputStream.write((byte) Integer.parseInt(binary, 2));
					contentSize += 1;
					for (Byte bt : bytes) {
						outputStream.write(bt);
						contentSize += 1;
					}
				}
				c = readed;
				l = 1;
			}
		} catch (IOException e) {
			System.out.println("cannot read bytes from input stream");
		}
		return new Output(contentSize, outputStream, 150);
	}

	@Override
	public Output parse(InputStream inputStream, int size) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		int counter = 0;
		while (counter < size) {
			int readed = inputStream.read();
			counter += 1;
			if (readed == -1)
				throw new IOException("cannot read byte");
			String binary =
					String.format("%8s", Integer.toBinaryString(readed & 0xFF)).replace(' ', '0');
			if (binary.startsWith("0")) {
				for (int j = 0; j < readed; j++) {
					outputStream.write(inputStream.read());
					counter ++;
				}
			} else {
				int count = Integer.parseInt("0" + binary.substring(1), 2);
				readed = inputStream.read();
				counter ++;
				for (int j = 0; j < count; j++) {
					outputStream.write(readed);
				}
			}
		}
		return new Output(-1, outputStream, -1);
	}

}
