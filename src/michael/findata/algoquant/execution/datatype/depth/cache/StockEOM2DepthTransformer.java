package michael.findata.algoquant.execution.datatype.depth.cache;

import com.numericalmethod.algoquant.execution.datatype.depth.Depth;
import com.numericalmethod.nmutils.iterator.DataTransformer;
import michael.findata.algoquant.execution.datatype.StockEOM;
import michael.findata.external.SecurityTimeSeriesDatum;
import michael.findata.model.Stock;

public class StockEOM2DepthTransformer implements DataTransformer<StockEOM, Depth> {
	public static enum Type {

		/** The source returns "open" price of the EOM. */
		OPEN {
			@Override
			public double transform(StockEOM eom) {
				return eom.open();
			}
		},
		/** The source returns the "close" price
		 * of EOM. */
		CLOSE {
			@Override
			public double transform(StockEOM eom) {
				return eom.close();
			}
		};
//		/** The source returns the
//		 * "adjusted close" price of EOD. */
//		ADJ_CLOSE {
//			@Override
//			public double transform(StockEOM eom) {
//				return eom.adjClose();
//			}
//		};

		public abstract double transform(StockEOM eod);
	}

	private final Stock stock;

	private final Type type;

	/**
	 * The bid and ask prices in the converted depth are the same.
	 *
	 * @param stock the stock
	 * @param type  the conversion type
	 */
	public StockEOM2DepthTransformer(Stock stock, Type type) {
		this.stock = stock;
		this.type = type;
	}

	@Override
	public Depth transform(StockEOM stockEOM) {
		double price = type.transform(stockEOM);
		boolean traded = true;
		if (stockEOM instanceof SecurityTimeSeriesDatum) {
			traded = ((SecurityTimeSeriesDatum) stockEOM).isTraded();
		}
		michael.findata.algoquant.execution.datatype.depth.Depth depth =
				new michael.findata.algoquant.execution.datatype.depth.Depth(price, stock, traded, price, price);
		depth.setVols(stockEOM.volume(), stockEOM.volume());
//		Depth d = new Depth(stock, new double[]{price, price}, depth);
		return depth;
	}
}