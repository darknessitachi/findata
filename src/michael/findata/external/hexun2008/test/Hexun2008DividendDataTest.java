package michael.findata.external.hexun2008.test;

import michael.findata.external.SecurityDividendData;
import michael.findata.external.hexun2008.Hexun2008DataException;
import michael.findata.external.hexun2008.Hexun2008DividendData;

import java.util.Date;

public class Hexun2008DividendDataTest {
	public static void main (String [] args) throws Hexun2008DataException {
		SecurityDividendData sdd = new Hexun2008DividendData("200726");
		for (Date d : sdd.getDividendRecords().keySet()) {
			System.out.print(d);
			System.out.print("\t");
			System.out.print(sdd.getDividendRecords().get(d).getAmount());
			System.out.print("\t");
			System.out.print(sdd.getDividendRecords().get(d).getBonus());
			System.out.print("\t");
			System.out.print(sdd.getDividendRecords().get(d).getBonus2());
			System.out.print("\t");
			System.out.print(sdd.getDividendRecords().get(d).getPaymentDate());
			System.out.print("\t");
			System.out.println(sdd.getDividendRecords().get(d).getTotal_amount());
		}
	}
}
