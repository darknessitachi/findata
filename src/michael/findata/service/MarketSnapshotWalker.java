package michael.findata.service;

import michael.findata.external.netease.NeteaseInstantSnapshot;
import michael.findata.util.FinDataConstants;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.*;

public class MarketSnapshotWalker {
	public static void main (String [] args) throws IOException {
		FileInputStream fis = new FileInputStream("E:/2016_04_05.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		FileWriter fw = new FileWriter("e:/temp.txt");
		String line;
		DateTime dateTime;
		NeteaseInstantSnapshot snapshot;
		int indexOfPipe;
		org.joda.time.format.DateTimeFormatter formatter = DateTimeFormat.forPattern(FinDataConstants.yyyyMMDDHHmmss);
		while ((line = br.readLine()) != null) {
			indexOfPipe = line.indexOf("|");
			dateTime = formatter.parseDateTime(line.substring(0, indexOfPipe));
			snapshot = new NeteaseInstantSnapshot(line.substring(indexOfPipe+1, line.length()));
//			for (Depth d : snapshot.getDepths()) {
//				d.spotPrice() < d.ask()
//			}
			System.out.println();
		}
	}
}