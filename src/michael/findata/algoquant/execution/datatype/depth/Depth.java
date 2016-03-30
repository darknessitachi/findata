package michael.findata.algoquant.execution.datatype.depth;

import com.numericalmethod.algoquant.execution.datatype.product.Product;

/**
 * Created by nicky on 2015/11/18.
 */
public class Depth extends com.numericalmethod.algoquant.execution.datatype.depth.Depth {
	/**
	 * @param product
	 * @param prices  bids then asks in ascending order, e.g., bid3, bid2, bid1, ask1, ask2, ask3
	 */
	public Depth(double spotPrice, Product product, double... prices) {
		super(product, prices);
		this.bidVol = new long[nLevels()];
		this.askVol = new long[nLevels()];
		this.spotPrice = spotPrice;
	}

	/**
	 * @param product
	 * @param price   bid is the same as ask, mainly used in simple simulation
	 */
	public Depth(double spotPrice, Product product, double price) {
		super(product, price);
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

	public long bidVol(int level) {
		return bidVol[level - 1];
	}

	public long askVol(int level) {
		return askVol[level - 1];
	}

	public double spotPrice () {
		return spotPrice;
	}
}