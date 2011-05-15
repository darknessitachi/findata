package michael.findata.external;

/**
 * Created by IntelliJ IDEA.
 * User: michaelc
 * Date: 2010-12-23
 * Time: 18:38:59
 * To change this template use File | Settings | File Templates.
 */
public abstract class FinancialSheetURLFactory {

	protected final String stockCode, sheetType, accountingPeriod;

	public final String getStockCode() {
		return stockCode;
	}

	public final String getSheetType() {
		return sheetType;
	}

	public final String getAccountingPeriod() {
		return accountingPeriod;
	}

	public FinancialSheetURLFactory(String stockCode, String sheetType, String accountingPeriod) {
		this.stockCode = stockCode;
		this.sheetType = sheetType;
		this.accountingPeriod = accountingPeriod;
	}

	public abstract String getURL();
}
