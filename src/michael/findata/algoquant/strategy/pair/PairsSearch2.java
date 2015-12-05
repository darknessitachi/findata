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

		stockList.add(new SimpleStock("000001.SZ", "ƽ������", Currencies.CNY, Exchange.SZSE));
		stockList.add(new SimpleStock("002142.SZ", "��������", Currencies.CNY, Exchange.SZSE));
		stockList.add(new SimpleStock("600000.SS", "�ַ�����", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("600015.SS", "��������", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("600016.SS", "��������", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("600036.SS", "��������", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601009.SS", "�Ͼ�����", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601166.SS", "��ҵ����", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601169.SS", "��������", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601288.SS", "ũҵ����", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601328.SS", "��ͨ����", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601398.SS", "��������", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601818.SS", "�������", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601939.SS", "��������", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601988.SS", "�й�����", Currencies.CNY, Exchange.SHSE));
		stockList.add(new SimpleStock("601998.SS", "��������", Currencies.CNY, Exchange.SHSE));

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
