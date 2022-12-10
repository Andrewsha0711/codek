package codek.algorithm;

import java.io.IOException;
import java.io.InputStream;

/**
 * Algorithm
 */
public interface Algorithm {
	public Output algorithm(InputStream inputStream) throws IOException;
	public Output parse(InputStream inputStream, int size) throws IOException;
}
