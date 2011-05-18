package michael.findata.external.hexun2008.test;

import michael.findata.external.hexun2008.Hexun2008DataException;
import michael.findata.external.hexun2008.Hexun2008ShareNumberDatum;

public class Hexun2008ShareNumberDatumTest {
	public static void main (String [] args) throws Hexun2008DataException {
		System.out.print(new Hexun2008ShareNumberDatum ("900929").getValue());
	}
}
