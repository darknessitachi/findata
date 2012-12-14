package michael.findata.external;

import michael.findata.util.FinDataConstants;

import java.util.Iterator;

import static michael.findata.util.FinDataConstants.SheetType;

public abstract class FinancialSheet {
	public abstract Iterator<String> getDatumNames();

	public abstract Number getValue(String name);

	public abstract String getURL();

	public abstract String getName();

	protected String stockCode;
	protected SheetType sheetType;
	protected int accountingYear = 0;
	protected short accountingSeason;

	public final String getStockCode() {
		return stockCode;
	}

	public final SheetType getSheetType() {
		return sheetType;
	}

	public final int getAccountingYear() {
		return accountingYear;
	}

	public final int getAccountingSeason() {
		return accountingYear;
	}

	public FinancialSheet(String stockCode, SheetType sheetType, int accountingYear, short accountingSeason) {
		this.stockCode = stockCode;
		this.sheetType = sheetType;
		this.accountingYear = accountingYear;
		this.accountingSeason = accountingSeason;
	}
}