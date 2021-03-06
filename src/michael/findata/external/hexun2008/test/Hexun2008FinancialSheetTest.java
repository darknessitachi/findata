package michael.findata.external.hexun2008.test;

import michael.findata.external.FinancialSheet;
import michael.findata.external.hexun2008.Hexun2008FinancialSheet;

import static michael.findata.util.FinDataConstants.SheetType.*;

public class Hexun2008FinancialSheetTest {
	public static void main(String[] args) {
		FinancialSheet sheet = new Hexun2008FinancialSheet("601101", profit_and_loss, 2015, (short)2);
		java.util.Iterator<String> it = sheet.getDatumNames();
		String name;
		while (it.hasNext()) {
			name = it.next();
			System.out.println(name + "\t" + sheet.getValue(name));
		}
	}
}
