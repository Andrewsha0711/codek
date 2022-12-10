import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import codek.algorithm.Output;
import codek.algorithm.Rle;

/**
 * RleTest
 */
public class RleTest {
	Rle algorithm = new Rle();

	@Test
	public void parseTest() {
		String text =
				"dhsjadhjsahdjsahdjaghwjhhdjshadjhasjdhjajkdjsajhdjasgdhgfshjdjagdhasjkdghjadhsghadh";
		try {
			byte[] txtBytes = text.getBytes();
			InputStream stream = new ByteArrayInputStream(txtBytes);
			Output output = this.algorithm.algorithm(stream);

			Assertions.assertEquals(output.outputStream.toByteArray().length, output.contentSize, "message");

			InputStream streamToParse = new ByteArrayInputStream(output.outputStream.toByteArray());
			Output parsed = this.algorithm.parse(streamToParse, output.contentSize);

			byte[] resBytes = parsed.outputStream.toByteArray();

			for (int i = 0; i < txtBytes.length; i++) {
				System.out.print(txtBytes[i] + " ");
			}
			System.out.println();
			for (int i = 0; i < resBytes.length; i++) {
				System.out.print(resBytes[i] + " ");
			}
			stream.close();
			streamToParse.close();
			Assertions.assertArrayEquals(resBytes, txtBytes, "message");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

