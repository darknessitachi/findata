package michael.findata.algoquant.product.stock.szse;

import com.numericalmethod.algoquant.execution.datatype.product.fx.Currencies;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Exchange;
import com.numericalmethod.algoquant.execution.datatype.product.stock.SimpleStock;

import java.util.Map;

import static com.numericalmethod.nmutils.collection.CollectionUtils.newHashMap;

/**
 * Created by nicky on 2015/8/12.
 */
public class SZSEStock extends SimpleStock {

	private static final Map<String, SZSEStock> map = newHashMap();

	/**
	 * Retrieves a {@linkplain SZSEStock} and avoids creating many "small" copies.
	 *
	 * @param symbol a stock symbol listed on the HKEX
	 * @return the {@linkplain SZSEStock} instance
	 * @see "http://en.wikipedia.org/wiki/Flyweight_pattern"
	 */
	public static SZSEStock newInstance(String symbol) {
		return newInstance(symbol, COMPANY_NAME_NA);
	}

	/**
	 * Retrieves a {@linkplain SZSEStock} and avoids creating many "small" copies.
	 *
	 * @param symbol      a stock symbol listed on the HKEX
	 * @param companyName a company name
	 * @return the {@linkplain SZSEStock} instance
	 * @see "http://en.wikipedia.org/wiki/Flyweight_pattern"
	 */
	public static SZSEStock newInstance(String symbol, String companyName) {
		SZSEStock stock = map.get(symbol);
		if (stock == null) {
			stock = new SZSEStock(symbol, companyName);
			map.put(symbol, stock);
		}
		return stock;
	}

	@Deprecated
	public SZSEStock(String symbol, String companyName) {
		// symbol is according to yahoo finance
		super(symbol.length() == 6? symbol+".SZ" : symbol,
				companyName,
				Currencies.CNY,
				Exchange.SHSE);
	}

	@Deprecated
	public SZSEStock(String symbol) {
		this(symbol,
				COMPANY_NAME_NA);
	}

}
