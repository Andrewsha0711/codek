package codek.v2;

public class Signature {
	protected static final byte[] byteSignature = {Byte.parseByte("41", 16),
			Byte.parseByte("42", 16), Byte.parseByte("43", 16), Byte.parseByte("44", 16)};
	protected static final int version = 2;
	protected static final int contextCompression = 0;
	protected static final int noContextCompression = 1;
	protected static final int interferenceProtection = 0;
	protected static final int size = 4;
	protected static final int nameSize = 1;
	protected static final int headersSize = byteSignature.length + 4 + size + nameSize;
}
