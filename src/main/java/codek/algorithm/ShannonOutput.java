package codek.algorithm;

import java.io.ByteArrayOutputStream;

/**
 * ShannonOutput
 */
public class ShannonOutput extends Output {
	// binary code map created for bytes in binary string
	public final String binaryCodeMap;

	public ShannonOutput(int contentSize, ByteArrayOutputStream outputStream, int profit,
			String binaryCodeMap) {
		super(contentSize, outputStream, profit);
		this.binaryCodeMap = binaryCodeMap;
	}
}
