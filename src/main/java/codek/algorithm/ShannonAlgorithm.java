package codek.algorithm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ShannonAlgorithm implements Algorithm {

	// creates map of byte codes
	public static Map<Byte, String> createMap(Map<Byte, Integer> sortedByValuesMap,
			List<Byte> fileBytes) {
		Map<Byte, String> codeMap = new HashMap<>();
		double pSum = 0;
		for (Map.Entry<Byte, Integer> entry : sortedByValuesMap.entrySet()) {
			double p = (double) entry.getValue() / (double) fileBytes.size();
			int digitCount = (int) (Math.ceil(-Math.log(p) / Math.log(2)));
			String code = "";
			double temp = pSum;
			for (int i = 0; i < digitCount; i++) {
				temp = temp * 2;
				code += (int) Math.floor(temp);
				temp = Double.parseDouble("0." + String.valueOf(temp).split("\\.")[1]);
			}
			pSum += p;
			codeMap.put(entry.getKey(), code);
		}
		return codeMap;
	}

	public static String readBinaryMap(byte[] binaryMapBytes) {
		String binaryMap = "";
		for (int i = 0; i < binaryMapBytes.length; i++) {
			String bt = String.format("%8s", Integer.toBinaryString(binaryMapBytes[i] & 0xFF))
					.replace(' ', '0');
			binaryMap += bt;
		}
		return binaryMap;
	}

	// creates binary string representation of code map
	public static String createBinaryMap(Map<Byte, String> codeMap) {
		String binaryMap = "";
		for (Entry<Byte, String> entry : codeMap.entrySet()) {
			String bt = entry.getKey() < 0 ? Integer.toBinaryString(entry.getKey() + 256)
					: Integer.toBinaryString(entry.getKey());
			while (bt.length() != 8) {
				bt = "0" + bt;
			}
			String codeSize = Integer.toBinaryString(entry.getValue().length());
			while (codeSize.length() != 8) {
				codeSize = "0" + codeSize;
			}
			binaryMap += bt + codeSize + entry.getValue();
		}
		while (binaryMap.length() % 8 != 0) {
			binaryMap += "0";
		}
		return binaryMap;
	}

	@Override
	public Output algorithm(InputStream sourceStream) throws IOException {

		List<Byte> fileBytes = new ArrayList<>();
		Map<Byte, Integer> bytesCountMap = new HashMap<>();
		int readed = 0;

		// read file bytes from stream to collection
		try {
			while ((readed = sourceStream.read()) != -1) {
				fileBytes.add((byte) readed);
				// save to count map
				Integer byteCount = bytesCountMap.get((byte) readed);
				if (byteCount == null) {
					bytesCountMap.put((byte) readed, 1);
				} else {
					bytesCountMap.put((byte) readed, byteCount + 1);
				}
			}
		} catch (IOException e) {
			System.out.println("cannot read bytes from input stream");
		}

		// bytes count map sorted by count
		LinkedHashMap<Byte, Integer> sortedByValuesMap = new LinkedHashMap<>();
		bytesCountMap.entrySet().stream().sorted(Entry.comparingByValue(Comparator.reverseOrder()))
				.forEachOrdered(e -> sortedByValuesMap.put(e.getKey(), e.getValue()));

		String binaryCode = "";
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		Map<Byte, String> codeMap = createMap(sortedByValuesMap, fileBytes);

		// create and write binary map
		String binaryMap = createBinaryMap(codeMap);
		byte binaryMapSizeBytes[] = ByteBuffer.allocate(4).putInt(binaryMap.length() / 8).array();
		out.write(binaryMapSizeBytes);
		int newBytesCount = 4;
		for (int i = 0; i < binaryMap.length(); i += 8) {
			int tmp = Integer.parseInt(binaryMap.substring(i, i + 8), 2);
			out.write((byte) tmp);
			newBytesCount++;
		}

		// writing coded content
		for (Byte s : fileBytes) {
			binaryCode += codeMap.get(s);
			while (binaryCode.length() > 8) {
				out.write((byte) Integer.parseInt(binaryCode.substring(0, 8), 2));
				binaryCode = binaryCode.substring(8);
				newBytesCount++;
			}
		}

		while (binaryCode.length() % 8 != 0) {
			binaryCode += "0";
		}
		for (int i = 0; i < binaryCode.length(); i += 8) {
			out.write((byte) Integer.parseInt(binaryCode.substring(i, i + 8), 2));
			newBytesCount++;
		}

		int size;
		int profit;
		if (newBytesCount < fileBytes.size()) {
			size = newBytesCount;
			profit = fileBytes.size() - newBytesCount;
		} else {
			out.reset();
			for (Byte bt : fileBytes) {
				out.write(bt);
			}
			size = fileBytes.size();
			profit = 0;
		}
		return new ShannonOutput(size, out, profit, binaryMap);
	}

	@Override
	public Output parse(InputStream inputStream, int size) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Map<String, Byte> codeMap = new HashMap<>();
		byte binaryMapSizeBytes[] = inputStream.readNBytes(4);
		int binaryMapSize = ByteBuffer.wrap(binaryMapSizeBytes).getInt();
		byte[] binaryMapBytes = inputStream.readNBytes(binaryMapSize);
		String binaryMap = readBinaryMap(binaryMapBytes);
		String binaryMapCopy = binaryMap;

		while (binaryMap.length() > 8) {
			try {
				byte bt = (byte) Integer.parseInt(binaryMap.substring(0, 8), 2);
				binaryMap = binaryMap.substring(8);
				int codeSize = Integer.parseInt(binaryMap.substring(0, 8), 2);
				binaryMap = binaryMap.substring(8);
				String code = binaryMap.substring(0, codeSize);
				binaryMap = binaryMap.substring(codeSize);
				codeMap.put(code, bt);
			} catch (Exception e) {
			}
		}

		int readed;
		String text = "";
		int i;
		for (i = 0; i < size - 4 - binaryMapSize; i++) {
			readed = inputStream.read();
			String binary =
					String.format("%8s", Integer.toBinaryString(readed & 0xFF)).replace(' ', '0');
			text += binary;
			text = findCode(codeMap, outputStream, text);
		}
		return new ShannonOutput(0, outputStream, 0, binaryMapCopy);
	}

	public static String findCode(Map<String, Byte> map, OutputStream outputStream, String text) {
		boolean flag = true;
		while (flag) {
			for (Map.Entry<String, Byte> code : map.entrySet()) {
				if (text.length() >= code.getKey().length() && text.startsWith(code.getKey())) {
					text = text.substring(text.indexOf(code.getKey()) + code.getKey().length());
					try {
						outputStream.write(code.getValue());
					} catch (Exception e) {
						e.printStackTrace();
					}
					flag = true;
					break;
				}
				flag = false;
			}
		}
		return text;
	}
}
