package michael.findata.algoquant.execution.datatype.depth;

import com.numericalmethod.algoquant.execution.datatype.product.Product;

public class Depth extends com.numericalmethod.algoquant.execution.datatype.depth.Depth {
	/**
	 * @param product
	 * @param traded
	 * @param prices  bids then asks in ascending order, e.g., bid3, bid2, bid1, ask1, ask2, ask3
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
		super(product, price);
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
}