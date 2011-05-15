package michael.findata.external;

/**
 * Created by IntelliJ IDEA.
 * User: michaelc
 * Date: 2010-11-17
 * Time: 18:14:18
 * To change this template use File | Settings | File Templates.
 */
public abstract class SecurityTradingDatum {

	public String getStockCode() {
		return null;
	}

	public String getStockName() {
		return null;
	}

	public Double getCurrent() {
		return -1d;
	}
}
