package michael.findata.util;

/**
 * Created by nicky on 2015/11/19.
 */
public class CalculationUtil {

	// if toSell is false then it means to buy
	// return -1 means that current depth cannot provide enough amount to buy/sell
	public static double findBestPrice (long quantity, boolean toSell, michael.findata.algoquant.execution.datatype.depth.Depth depth) {
		double temp = 0;
		for (int j = 1; j <= depth.nLevels(); j ++) {
			temp += toSell ? depth.bidVol(j) : depth.askVol(j);
			if (temp >= quantity) {
				return toSell ? depth.bid(j) : depth.ask(j);
			}
		}
		return -1;
	}
}