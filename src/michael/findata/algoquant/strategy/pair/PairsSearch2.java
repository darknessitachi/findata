package michael.findata.algoquant.strategy.pair;

import com.numericalmethod.algoquant.execution.datatype.product.fx.Currencies;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Exchange;
import com.numericalmethod.algoquant.execution.datatype.product.stock.SimpleStock;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Stock;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import java.util.ArrayList;

/**
 * Created by nicky on 2015/11/25.
 */
public class PairsSearch2 {
	public static void main (String [] args) {

		ArrayList<Stock> stockList = new ArrayList<>();

		stockList.add(new SimpleStock("000001.SZ", "平安银行", Currencies.CNY, Exchange.SZSE));
		stockList.add(new SimpleStock("002142.SZ", "宁波银行", Currencies.CNY, Exchange.SZSE));
		stockList.add(new SimpleStock("600000.SS", "浦发银行", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("600015.SS", "华夏银行", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("600016.SS", "民生银行", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("600036.SS", "招商银行", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601009.SS", "南京银行", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601166.SS", "兴业银行", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601169.SS", "北京银行", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601288.SS", "农业银行", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601328.SS", "交通银行", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601398.SS", "工商银行", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601818.SS", "光大银行", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601939.SS", "建设银行", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601988.SS", "中国银行", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601998.SS", "中信银行", Currencies.CNY, Exchange.SHSE));

		// Correlation Search - Pearson's test
		PearsonsCorrelation correl = new PearsonsCorrelation();
//		for ()
//		Stock shortEnd = ;
//		Stock longEnd = ;
//
//		correl.correlation();
		// identify any pair that has correlation > 0.9

		// Cointegration Test
	}
}
