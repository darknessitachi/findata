package michael.findata.external.hexun2008.test;

import michael.findata.external.hexun2008.Hexun2008FundDividendData;

import java.util.Date;

/**
 * Created by nicky on 2016/3/27.
 */
public class Hexun2008FundDividendDataTest {
	public static void main (String [] args) {
		Hexun2008FundDividendData sdd = new Hexun2008FundDividendData("510180");
		for (Date d : sdd.getDividendRecords().keySet()) {
			System.out.print(d);
			System.out.print("\t");
			System.out.print(sdd.getDividendRecords().get(d).getAmount());
			System.out.print("\t");
			System.out.print(sdd.getDividendRecords().get(d).getBonus());
			System.out.print("\t");
			System.out.print(sdd.getDividendRecords().get(d).getSplit());
			System.out.print("\t");
			System.out.print(sdd.getDividendRecords().get(d).getPaymentDate());
			System.out.print("\t");
			System.out.println(sdd.getDividendRecords().get(d).getTotal_amount());
		}
		sdd = new Hexun2008FundDividendData("510050");
		for (Date d : sdd.getDividendRecords().keySet()) {
			System.out.print(d);
			System.out.print("\t");
			System.out.print(sdd.getDividendRecords().get(d).getAmount());
			System.out.print("\t");
			System.out.print(sdd.getDividendRecords().get(d).getBonus());
			System.out.print("\t");
			System.out.print(sdd.getDividendRecords().get(d).getSplit());
			System.out.print("\t");
			System.out.print(sdd.getDividendRecords().get(d).getPaymentDate());
			System.out.print("\t");
			System.out.println(sdd.getDividendRecords().get(d).getTotal_amount());
		}
	}
}
