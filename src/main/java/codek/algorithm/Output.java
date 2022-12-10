package codek.algorithm;

import java.io.ByteArrayOutputStream;

public class Output {
	public final int contentSize;
	public final ByteArrayOutputStream outputStream;
	public final int profit;

	public Output(int contentSize, ByteArrayOutputStream outputStream, int profit) {
		this.contentSize = contentSize;
		this.outputStream = outputStream;
		this.profit = profit;
	}
}
