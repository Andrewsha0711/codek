package codek;

import java.io.ByteArrayOutputStream;

public interface Codek {
	public ByteArrayOutputStream pack(String absolutePath, String root,
			ByteArrayOutputStream outputStream, boolean headers);

	public void unpack(String filePath, String target);
}
