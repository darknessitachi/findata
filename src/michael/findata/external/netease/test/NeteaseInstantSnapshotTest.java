package michael.findata.external.netease.test;

import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import michael.findata.external.netease.NeteaseInstantSnapshot;

/**
 * Created by nicky on 2015/8/18.
 */
public class NeteaseInstantSnapshotTest {
	public static void main (String [] args) {
		String [] codes = {"600000", "510050" , "159919"};
		NeteaseInstantSnapshot snapshot = new NeteaseInstantSnapshot(codes);
		System.out.println();
	}
}
