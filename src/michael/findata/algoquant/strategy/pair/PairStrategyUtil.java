package michael.findata.algoquant.strategy.pair;

import org.apache.commons.math3.util.FastMath;

public class PairStrategyUtil {

	/**
	 * Calculate the most suitable long/short volume
	 * to minimize the amount difference between short/long
	 *
	 * @param bidPrice
	 * @param bidVol total volume available at or above bidPrice
	 * @param askPirce
	 * @param askVol total volume available at or below askPrice
	 * @param balanceOption
	 * @param maxDeltaPctg a series of percentage threshold to try, must be in ascending order
	 * @return	first number is volume to short
	 * 			second number is volume to long
	 */
	public static double[] calVolumes(double bidPrice, double bidVol, int bidLotSize,
									  double askPirce, double askVol, int askLotSize,
									  double amountUpperLimit, double amountLowerLimit,
									  BalanceOption balanceOption, double... maxDeltaPctg) {
		double v1, v2;
		double askAmount;
		double delta;
		if (bidPrice * bidVol < askPirce * askVol) {
			double[] result = calVolumes(askPirce, askVol, askLotSize, bidPrice, bidVol, bidLotSize, amountUpperLimit, amountLowerLimit, balanceOption.opposite(), maxDeltaPctg);
			if (result == null) {
				return null;
			} else {
				return new double[]{result[1], result[0]};
			}
		} else {
			for (double maxDelta : maxDeltaPctg) {
				for (double askLots = FastMath.min(FastMath.floor(askVol/askLotSize), FastMath.floor(amountUpperLimit / (askLotSize * askPirce)));
					 askLots > 0;
					 askLots -= 1) {
					v1 = askLots * askLotSize;
					askAmount = v1 * askPirce;
					if (askAmount < amountLowerLimit) {
						break;
					}
					switch (balanceOption) {
						case CLOSEST_MATCH:
							v2 = Math.round(askAmount / bidPrice / bidLotSize) * bidLotSize;
							break;
						case SHORT_LARGER:
							v2 = Math.ceil(askAmount / bidPrice / bidLotSize) * bidLotSize;
							break;
						default:
							v2 = Math.floor(askAmount / bidPrice / bidLotSize) * bidLotSize;
					}
					delta = 1 - (askPirce * v1) / (bidPrice * v2);
					if (delta < maxDelta && delta > -maxDelta && v2 <= bidVol) {
						return new double[]{v2, v1};
					}
				}
			}
		}
		return null;
	}

	public enum BalanceOption {
		CLOSEST_MATCH,	// buy more or sell more whichever option makes the most balanced trade
		SHORT_LARGER,	// always sell a little more than buy
		LONG_LARGER;	// always buy a little more than sell

		public BalanceOption opposite() {
			switch (this) {
				case CLOSEST_MATCH:
					return CLOSEST_MATCH;
				case SHORT_LARGER:
					return LONG_LARGER;
				default:
					return SHORT_LARGER;
			}
		}
	}
}