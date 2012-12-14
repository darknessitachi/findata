package michael.findata.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class StringParserUtil {
	public static String skipByCharacteristicStrings (BufferedReader r, String [] characteristicStrings) throws IOException {
		String line = r.readLine();
		int index = 0;
		while (line != null && index < characteristicStrings.length) {
			if (line.contains(characteristicStrings[index])) index ++;
			line = r.readLine();
//				System.out.println(line);
		}
		return line;
	}
}
