package michael.findata.algoquant.execution.datatype.depth.test;

import com.numericalmethod.algoquant.execution.datatype.product.fx.Currencies;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Exchange;
import com.numericalmethod.algoquant.execution.datatype.product.stock.SimpleStock;
import michael.findata.algoquant.execution.datatype.depth.Depth;

public class DepthTest {
	public static void main (String [] args) {
		Depth depth = new Depth(13.21,
				new SimpleStock("600000", "ÆÖ·¢ÒøÐÐ", Currencies.CNY, Exchange.SHSE), true,
				12.11, 12.12, 12.13, 12.14, 12.15, 12.16, 12.17, 12.18, 12.19, 12.20);
		depth.setVols(1000, 2000, 3000, 4000, 1000, 2000, 3000, 4000, 1000, 2000);
		System.out.println(depth.bestAsk(50000));
		System.out.println(depth.bestBid(50000));
		System.out.println(depth.totalAskAtOrBelow(depth.bestAsk(50000)));
		System.out.println(depth.totalBidAtOrAbove(depth.bestBid(50000)));
	}
}
