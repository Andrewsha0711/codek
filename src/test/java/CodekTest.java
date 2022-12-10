import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import codek.algorithm.Output;
import codek.algorithm.ShannonAlgorithm;
import codek.algorithm.ShannonOutput;

public class CodekTest {
	ShannonAlgorithm algorithm = new ShannonAlgorithm();

    @Test
    public void test() {
        int a1 = -105;
        int a2 = 150;
        String str = Integer.toBinaryString(a1 + 256);
        String str1 = Integer.toBinaryString(a2);
        int b = Integer.valueOf(str, 2);
        int b1 = Integer.valueOf(str1, 2);
        byte bt = (byte) b;
        byte bt1 = (byte) b1;
        // Assertions.assertEquals(a2, bt1, "a2!=bt1");
        Assertions.assertEquals(a1, bt, "a1!=bt");
    }

    @Test
    public void parseMapTest() {
        String text = "dhsjadhjsahdjsahdjaghwjhhdjshadjhasjdhjajkdjsajhdjasgdhgfshjdjagdhasjkdghjadhsghadh";
        try {
            InputStream stream = new ByteArrayInputStream(text.getBytes());
            Output output = this.algorithm.algorithm(stream);
            String binaryMap = ((ShannonOutput) output).binaryCodeMap;
            byte[] bytes = new byte[binaryMap.length() / 8];
            for (int i = 0; i < binaryMap.length(); i += 8) {
                int tmp = Integer.valueOf(binaryMap.substring(i, i + 8), 2);
                bytes[i / 8] = (byte) tmp;
            }
            String newMap = "";
            for (int i = 0; i < bytes.length; i++) {
                String code = String.format("%8s", Integer.toBinaryString(bytes[i] & 0xFF)).replace(' ', '0');
                newMap += code;
            }
            System.out.println("binary map from create binary map:\n" + binaryMap);
            System.out.println(newMap);
            Assertions.assertTrue(binaryMap.equals(newMap));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
