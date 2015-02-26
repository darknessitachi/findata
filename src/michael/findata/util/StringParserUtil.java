package michael.findata.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import static michael.findata.util.FinDataConstants.*;

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
	public static int inferSeason (String seasonString) throws ParseException {
		if (seasonString.contains(s1Report)) {
			return 1;
		} else if (seasonString.contains(s3Report)) {
			return 3;
		} else if (seasonString.contains(s2Report) || seasonString.contains(s2Report2)) {
			return 2;
		} else if (seasonString.contains(s4Report)) {
			return 4;
		} else {
			throw new ParseException(seasonString, 0);
		}
	}
}