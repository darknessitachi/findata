package michael.findata.external;

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
