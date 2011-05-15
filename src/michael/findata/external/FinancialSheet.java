package michael.findata.external;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: michaelc
 * Date: 2010-12-28
 * Time: 17:09:28
 * To change this template use File | Settings | File Templates.
 */
public abstract class FinancialSheet {
	public abstract Iterator<String> getDatumNames ();
	public abstract Number getValue(String name);
	public abstract String getURL ();
	public abstract String getName ();
//	public enum AccountingSeason {
//		first, second, third, fourth;
//	};

	protected String stockCode, sheetType;
	protected int accountingYear = 0;
	protected short accountingSeason;

	public final String getStockCode (){
		return stockCode;
	}

	public final String getSheetType () {
		return sheetType;
	}

	public final int getAccountingYear (){
		return accountingYear;
	}

	public final int getAccountingSeason (){
		return accountingYear;
	}

	public FinancialSheet (String stockCode, String sheetType, int accountingYear, short accountingSeason) {
		this.stockCode = stockCode;
		this.sheetType = sheetType;
		this.accountingYear = accountingYear;
		this.accountingSeason = accountingSeason;
	}
}