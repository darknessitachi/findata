package michael.findata.algoquant.product.stock.shse;

import com.numericalmethod.algoquant.execution.datatype.product.fx.Currencies;
import com.numericalmethod.algoquant.execution.datatype.product.stock.Exchange;
import com.numericalmethod.algoquant.execution.datatype.product.stock.SimpleStock;

import java.util.Map;

import static com.numericalmethod.nmutils.collection.CollectionUtils.newHashMap;

/**
 * Created by nicky on 2015/8/10.
 */
public class SHSEStock extends SimpleStock {

	private static final Map<String, SHSEStock> map = newHashMap();

	/**
	 * Retrieves a {@linkplain SHSEStock} and avoids creating many "small" copies.
	 *
	 * @param symbol a stock symbol listed on the HKEX
	 * @return the {@linkplain SHSEStock} instance
	 * @see "http://en.wikipedia.org/wiki/Flyweight_pattern"
	 */
	public static SHSEStock newInstance(String symbol) {
		return newInstance(symbol, COMPANY_NAME_NA);
	}

	/**
	 * Retrieves a {@linkplain SHSEStock} and avoids creating many "small" copies.
	 *
	 * @param symbol      a stock symbol listed on the HKEX
	 * @param companyName a company name
	 * @return the {@linkplain SHSEStock} instance
	 * @see "http://en.wikipedia.org/wiki/Flyweight_pattern"
	 */
	public static SHSEStock newInstance(String symbol, String companyName) {
		SHSEStock stock = map.get(symbol);
		if (stock == null) {
			stock = new SHSEStock(symbol, companyName);
			map.put(symbol, stock);
		}
		return stock;
	}

	@Deprecated
	public SHSEStock(String symbol, String companyName) {
		super(symbol.length() == 6? symbol+".SH" : symbol,
				companyName,
				Currencies.CNY,
				Exchange.SHSE);
	}

	@Deprecated
	public SHSEStock(String symbol) {
		this(symbol,
				COMPANY_NAME_NA);
	}

}
