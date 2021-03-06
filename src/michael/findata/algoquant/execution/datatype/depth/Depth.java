package michael.findata.algoquant.execution.datatype.depth;

import com.numericalmethod.algoquant.execution.datatype.product.Product;
import org.apache.commons.math3.util.FastMath;

public class Depth extends com.numericalmethod.algoquant.execution.datatype.depth.Depth {
	/**
	 * @param product
	 * @param traded
	 * @param prices bids then asks in ascending order, e.g., ... bid3, bid2, bid1, ask1, ask2, ask3 ...
	 */
	public Depth(double spotPrice, Product product, boolean traded, double... prices) {
		super(product, prices);
		this.traded = traded;
		this.bidVol = new long[nLevels()];
		this.askVol = new long[nLevels()];
		this.spotPrice = spotPrice;
	}

	/**
	 * @param product
	 * @param price   bid is the same as ask, mainly used in simple simulation
	 * @param traded
	 */
	public Depth(double spotPrice, Product product, boolean traded, double price) {
		super(product, price, price);
		this.traded = traded;
		this.bidVol = new long[nLevels()];
		this.askVol = new long[nLevels()];
		this.spotPrice = spotPrice;
	}

	public void setVols(long... vols){
		int nLevels = nLevels();
		for (int i = 0; i < nLevels; ++i) {
			this.bidVol[nLevels - i - 1] = vols[i];
		}
		System.arraycopy(vols, nLevels, this.askVol, 0, nLevels);
	}

	private final long[] bidVol;
	private final long[] askVol;
	private final double spotPrice;
	private final boolean traded;

	public long bidVol(int level) {
		return bidVol[level - 1];
	}

	public long askVol(int level) {
		return askVol[level - 1];
	}

	public double spotPrice () {
		return spotPrice;
	}

	public boolean isTraded() {
		return traded;
	}


	/**
	 * The best bid price one can get if one wants to sell at least amountThreshold amount of money
	 * @param amountThreshold
	 * @return meaningful amount of above zero, meaningless otherwise
	 */
	public double bestBid(double amountThreshold) {
		double amountAccumulated = 0d;
		int nlevels = nLevels();
		for (int i = 1; i <= nlevels; i++) {
			amountAccumulated += bid(i) * bidVol(i);
			if (amountAccumulated >= amountThreshold) {
				return bid(i);
			}
		}
		return -1000d; // meaning total exposed bid order cannot provide such an amount for one to sell
	}

	/**
	 * The best ask price one can get if one wants to buy at least amountThreshold amount of money
	 * @param amountThreshold
	 * @return meaningful amount of above zero, meaningless otherwise
	 */
	public double bestAsk(double amountThreshold) {
		double amountAccumulated = 0d;
		int nlevels = nLevels();
		for (int i = 1; i <= nlevels; i++) {
			amountAccumulated += ask(i) * askVol(i);
			if (amountAccumulated >= amountThreshold) {
				return ask(i);
			}
		}
		return -1000d; // meaning total exposed ask offer cannot provide such an amount for one to buy
	}

	/**
	 * Total volume one can sell at or above the priceThreshold bid price
	 * @param priceThreshold
	 * @return
	 */
	public int totalBidAtOrAbove(double priceThreshold) {
		int volumeAccumulated = 0;
		int nlevels = nLevels();
		for (int i = 1; i<= nlevels; i++) {
			if (bid(i) < priceThreshold) {
				break;
			}
			volumeAccumulated += bidVol(i);
		}
		return volumeAccumulated;
	}

	/**
	 * Total volume one can buy at or below the priceThreshold ask price
	 * @param priceThreshold
	 * @return
	 */
	public int totalAskAtOrBelow(double priceThreshold) {
		int volumeAccumulated = 0;
		int nlevels = nLevels();
		for (int i = 1; i<= nlevels; i++) {
			if (ask(i) > priceThreshold) {
				break;
			}
			volumeAccumulated += askVol(i);
		}
		return volumeAccumulated;
	}

	// % difference between the lowest bid and the given bid
	public double bidDepthDistance (double bid) {
		for (int i = nLevels(); i>= 1; i--) {
			if (bid(i) != 0) {
				return FastMath.abs(bid(i) - bid) / bid;
			}
		}
		return 1d;
	}

	// % difference between the highest ask and the given ask
	public double askDepthDistance (double ask) {
		for (int i = nLevels(); i>= 1; i--) {
			if (ask(i) != 0) {
				return FastMath.abs(ask(i) - ask) / ask;
			}
		}
		return 1d;
	}

	public String toString () {
		StringBuilder sb = new StringBuilder();
		int nlevels = nLevels();
		sb.append(product()).append("\n");
		sb.append("Traded: ").append(isTraded()).append("\n");
		if (nlevels >= 5) sb.append("ask5 ").append(ask(5)).append('\t').append(askVol(5)).append('\n');
		if (nlevels >= 4) sb.append("ask4 ").append(ask(4)).append('\t').append(askVol(4)).append('\n');
		if (nlevels >= 3) sb.append("ask3 ").append(ask(3)).append('\t').append(askVol(3)).append('\n');
		if (nlevels >= 2) sb.append("ask2 ").append(ask(2)).append('\t').append(askVol(2)).append('\n');
		if (nlevels >= 1) {
			sb.append("ask1 ").append(ask(1)).append('\t').append(askVol(1)).append('\n');
			sb.append("bid1 ").append(bid(1)).append('\t').append(bidVol(1)).append('\n');
		}
		if (nlevels >= 2) sb.append("bid2 ").append(bid(2)).append('\t').append(bidVol(2)).append('\n');
		if (nlevels >= 3) sb.append("bid3 ").append(bid(3)).append('\t').append(bidVol(3)).append('\n');
		if (nlevels >= 4) sb.append("bid4 ").append(bid(4)).append('\t').append(bidVol(4)).append('\n');
		if (nlevels >= 5) sb.append("bid5 ").append(bid(5)).append('\t').append(bidVol(5)).append('\n');
		return sb.toString();
	}
}