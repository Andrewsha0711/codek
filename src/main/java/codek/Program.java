package codek;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import codek.v2.CodekV2;

public class Program {
	private static final String actualVersion = "2";
	private static Codek codek = new CodekV2();

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("wrong arguments count");
			args = new String[3];
			// pack

			args[0] = "pack";
			args[1] = "/home/Andrewsha/workspace/temp/test";
			args[2] = "/home/Andrewsha/workspace/temp/oleg.oleg";

			// return;
		}
		if (args[0].equals("pack")) {
			String target = args[args.length - 1];
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			boolean headers = true;
			for (int i = 1; i < args.length - 1; i++) {
				String root = args[i].split("/")[args[i].split("/").length - 1];
				bytes = codek.pack(args[i], root, bytes, headers);
				headers = false;
			}
			try {
				FileOutputStream outputStream = new FileOutputStream(target);
				bytes.writeTo(outputStream);
				bytes.close();
				outputStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		args[0] = "unpack";
		args[1] = "/home/Andrewsha/workspace/temp/oleg.oleg";
		args[2] = "/home/Andrewsha/workspace/temp/test2";

		if (args[0].equals("unpack")) {
			codek.unpack(args[1], args[2]);
		}
	}
}
