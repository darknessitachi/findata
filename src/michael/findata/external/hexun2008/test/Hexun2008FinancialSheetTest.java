package michael.findata.external.hexun2008.test;

import michael.findata.external.FinancialSheet;
import michael.findata.external.hexun2008.Hexun2008FinancialSheet;
import michael.findata.util.FinDataConstants;

/**
 * Created by IntelliJ IDEA.
 * User: michaelc
 * Date: 2011-1-7
 * Time: 17:02:17
 * To change this template use File | Settings | File Templates.
 */
public class Hexun2008FinancialSheetTest {
	public static void main (String [] args) {
		FinancialSheet sheet = new Hexun2008FinancialSheet("600026", FinDataConstants.FINANCIAL_SHEET_BALANCE_SHEET, 2008, (short)4);
		java.util.Iterator<String> it = sheet.getDatumNames();
		String name;
		while (it.hasNext()) {
			name = it.next();
			System.out.println(name+"\t"+sheet.getValue(name));
		}
	}
}