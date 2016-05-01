package michael.findata.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@javax.persistence.Table(name = "balance_sheet_nf", schema = "", catalog = "findata")
@Entity
public class BalanceSheetNf {
	private int id;

	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	@javax.persistence.Column(name = "id", nullable = false, insertable = true, updatable = true, length = 10, precision = 0)
	@Id
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private int finYear;

	@javax.persistence.Column(name = "fin_year", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	public int getFinYear() {
		return finYear;
	}

	public void setFinYear(int finYear) {
		this.finYear = finYear;
	}

	private int finSeason;

	@javax.persistence.Column(name = "fin_season", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	public int getFinSeason() {
		return finSeason;
	}

	public void setFinSeason(int finSeason) {
		this.finSeason = finSeason;
	}

	private double bs01;

	@javax.persistence.Column(name = "bs01", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs01() {
		return bs01;
	}

	public void setBs01(double bs01) {
		this.bs01 = bs01;
	}

	private double bs02;

	@javax.persistence.Column(name = "bs02", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs02() {
		return bs02;
	}

	public void setBs02(double bs02) {
		this.bs02 = bs02;
	}

	private double bs03;

	@javax.persistence.Column(name = "bs03", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs03() {
		return bs03;
	}

	public void setBs03(double bs03) {
		this.bs03 = bs03;
	}

	private double bs04;

	@javax.persistence.Column(name = "bs04", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs04() {
		return bs04;
	}

	public void setBs04(double bs04) {
		this.bs04 = bs04;
	}

	private double bs05;

	@javax.persistence.Column(name = "bs05", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs05() {
		return bs05;
	}

	public void setBs05(double bs05) {
		this.bs05 = bs05;
	}

	private double bs06;

	@javax.persistence.Column(name = "bs06", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs06() {
		return bs06;
	}

	public void setBs06(double bs06) {
		this.bs06 = bs06;
	}

	private double bs07;

	@javax.persistence.Column(name = "bs07", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs07() {
		return bs07;
	}

	public void setBs07(double bs07) {
		this.bs07 = bs07;
	}

	private double bs08;

	@javax.persistence.Column(name = "bs08", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs08() {
		return bs08;
	}

	public void setBs08(double bs08) {
		this.bs08 = bs08;
	}

	private double bs09;

	@javax.persistence.Column(name = "bs09", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs09() {
		return bs09;
	}

	public void setBs09(double bs09) {
		this.bs09 = bs09;
	}

	private double bs10;

	@javax.persistence.Column(name = "bs10", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs10() {
		return bs10;
	}

	public void setBs10(double bs10) {
		this.bs10 = bs10;
	}

	private double bs11;

	@javax.persistence.Column(name = "bs11", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs11() {
		return bs11;
	}

	public void setBs11(double bs11) {
		this.bs11 = bs11;
	}

	private double bs12;

	@javax.persistence.Column(name = "bs12", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs12() {
		return bs12;
	}

	public void setBs12(double bs12) {
		this.bs12 = bs12;
	}

	private double bs13;

	@javax.persistence.Column(name = "bs13", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs13() {
		return bs13;
	}

	public void setBs13(double bs13) {
		this.bs13 = bs13;
	}

	private double bs14;

	@javax.persistence.Column(name = "bs14", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs14() {
		return bs14;
	}

	public void setBs14(double bs14) {
		this.bs14 = bs14;
	}

	private double bs15;

	@javax.persistence.Column(name = "bs15", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs15() {
		return bs15;
	}

	public void setBs15(double bs15) {
		this.bs15 = bs15;
	}

	private double bs16;

	@javax.persistence.Column(name = "bs16", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs16() {
		return bs16;
	}

	public void setBs16(double bs16) {
		this.bs16 = bs16;
	}

	private double bs17;

	@javax.persistence.Column(name = "bs17", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs17() {
		return bs17;
	}

	public void setBs17(double bs17) {
		this.bs17 = bs17;
	}

	private double bs18;

	@javax.persistence.Column(name = "bs18", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs18() {
		return bs18;
	}

	public void setBs18(double bs18) {
		this.bs18 = bs18;
	}

	private double bs19;

	@javax.persistence.Column(name = "bs19", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs19() {
		return bs19;
	}

	public void setBs19(double bs19) {
		this.bs19 = bs19;
	}

	private double bs20;

	@javax.persistence.Column(name = "bs20", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs20() {
		return bs20;
	}

	public void setBs20(double bs20) {
		this.bs20 = bs20;
	}

	private double bs21;

	@javax.persistence.Column(name = "bs21", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs21() {
		return bs21;
	}

	public void setBs21(double bs21) {
		this.bs21 = bs21;
	}

	private double bs22;

	@javax.persistence.Column(name = "bs22", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs22() {
		return bs22;
	}

	public void setBs22(double bs22) {
		this.bs22 = bs22;
	}

	private double bs23;

	@javax.persistence.Column(name = "bs23", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs23() {
		return bs23;
	}

	public void setBs23(double bs23) {
		this.bs23 = bs23;
	}

	private double bs24;

	@javax.persistence.Column(name = "bs24", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs24() {
		return bs24;
	}

	public void setBs24(double bs24) {
		this.bs24 = bs24;
	}

	private double bs25;

	@javax.persistence.Column(name = "bs25", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs25() {
		return bs25;
	}

	public void setBs25(double bs25) {
		this.bs25 = bs25;
	}

	private double bs26;

	@javax.persistence.Column(name = "bs26", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs26() {
		return bs26;
	}

	public void setBs26(double bs26) {
		this.bs26 = bs26;
	}

	private double bs27;

	@javax.persistence.Column(name = "bs27", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs27() {
		return bs27;
	}

	public void setBs27(double bs27) {
		this.bs27 = bs27;
	}

	private double bs28;

	@javax.persistence.Column(name = "bs28", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs28() {
		return bs28;
	}

	public void setBs28(double bs28) {
		this.bs28 = bs28;
	}

	private double bs29;

	@javax.persistence.Column(name = "bs29", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs29() {
		return bs29;
	}

	public void setBs29(double bs29) {
		this.bs29 = bs29;
	}

	private double bs30;

	@javax.persistence.Column(name = "bs30", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs30() {
		return bs30;
	}

	public void setBs30(double bs30) {
		this.bs30 = bs30;
	}

	private double bs31;

	@javax.persistence.Column(name = "bs31", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs31() {
		return bs31;
	}

	public void setBs31(double bs31) {
		this.bs31 = bs31;
	}

	private double bs32;

	@javax.persistence.Column(name = "bs32", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs32() {
		return bs32;
	}

	public void setBs32(double bs32) {
		this.bs32 = bs32;
	}

	private double bs33;

	@javax.persistence.Column(name = "bs33", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs33() {
		return bs33;
	}

	public void setBs33(double bs33) {
		this.bs33 = bs33;
	}

	private double bs34;

	@javax.persistence.Column(name = "bs34", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs34() {
		return bs34;
	}

	public void setBs34(double bs34) {
		this.bs34 = bs34;
	}

	private double bs35;

	@javax.persistence.Column(name = "bs35", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs35() {
		return bs35;
	}

	public void setBs35(double bs35) {
		this.bs35 = bs35;
	}

	private double bs36;

	@javax.persistence.Column(name = "bs36", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs36() {
		return bs36;
	}

	public void setBs36(double bs36) {
		this.bs36 = bs36;
	}

	private double bs37;

	@javax.persistence.Column(name = "bs37", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs37() {
		return bs37;
	}

	public void setBs37(double bs37) {
		this.bs37 = bs37;
	}

	private double bs38;

	@javax.persistence.Column(name = "bs38", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs38() {
		return bs38;
	}

	public void setBs38(double bs38) {
		this.bs38 = bs38;
	}

	private double bs39;

	@javax.persistence.Column(name = "bs39", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs39() {
		return bs39;
	}

	public void setBs39(double bs39) {
		this.bs39 = bs39;
	}

	private double bs40;

	@javax.persistence.Column(name = "bs40", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs40() {
		return bs40;
	}

	public void setBs40(double bs40) {
		this.bs40 = bs40;
	}

	private double bs41;

	@javax.persistence.Column(name = "bs41", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs41() {
		return bs41;
	}

	public void setBs41(double bs41) {
		this.bs41 = bs41;
	}

	private double bs42;

	@javax.persistence.Column(name = "bs42", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs42() {
		return bs42;
	}

	public void setBs42(double bs42) {
		this.bs42 = bs42;
	}

	private double bs43;

	@javax.persistence.Column(name = "bs43", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs43() {
		return bs43;
	}

	public void setBs43(double bs43) {
		this.bs43 = bs43;
	}

	private double bs44;

	@javax.persistence.Column(name = "bs44", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs44() {
		return bs44;
	}

	public void setBs44(double bs44) {
		this.bs44 = bs44;
	}

	private double bs45;

	@javax.persistence.Column(name = "bs45", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs45() {
		return bs45;
	}

	public void setBs45(double bs45) {
		this.bs45 = bs45;
	}

	private double bs46;

	@javax.persistence.Column(name = "bs46", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs46() {
		return bs46;
	}

	public void setBs46(double bs46) {
		this.bs46 = bs46;
	}

	private double bs47;

	@javax.persistence.Column(name = "bs47", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs47() {
		return bs47;
	}

	public void setBs47(double bs47) {
		this.bs47 = bs47;
	}

	private double bs48;

	@javax.persistence.Column(name = "bs48", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs48() {
		return bs48;
	}

	public void setBs48(double bs48) {
		this.bs48 = bs48;
	}

	private double bs49;

	@javax.persistence.Column(name = "bs49", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs49() {
		return bs49;
	}

	public void setBs49(double bs49) {
		this.bs49 = bs49;
	}

	private double bs50;

	@javax.persistence.Column(name = "bs50", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs50() {
		return bs50;
	}

	public void setBs50(double bs50) {
		this.bs50 = bs50;
	}

	private double bs51;

	@javax.persistence.Column(name = "bs51", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs51() {
		return bs51;
	}

	public void setBs51(double bs51) {
		this.bs51 = bs51;
	}

	private double bs52;

	@javax.persistence.Column(name = "bs52", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs52() {
		return bs52;
	}

	public void setBs52(double bs52) {
		this.bs52 = bs52;
	}

	private double bs53;

	@javax.persistence.Column(name = "bs53", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs53() {
		return bs53;
	}

	public void setBs53(double bs53) {
		this.bs53 = bs53;
	}

	private double bs54;

	@javax.persistence.Column(name = "bs54", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs54() {
		return bs54;
	}

	public void setBs54(double bs54) {
		this.bs54 = bs54;
	}

	private double bs55;

	@javax.persistence.Column(name = "bs55", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs55() {
		return bs55;
	}

	public void setBs55(double bs55) {
		this.bs55 = bs55;
	}

	private double bs56;

	@javax.persistence.Column(name = "bs56", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs56() {
		return bs56;
	}

	public void setBs56(double bs56) {
		this.bs56 = bs56;
	}

	private double bs57;

	@javax.persistence.Column(name = "bs57", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs57() {
		return bs57;
	}

	public void setBs57(double bs57) {
		this.bs57 = bs57;
	}

	private double bs58;

	@javax.persistence.Column(name = "bs58", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs58() {
		return bs58;
	}

	public void setBs58(double bs58) {
		this.bs58 = bs58;
	}

	private double bs59;

	@javax.persistence.Column(name = "bs59", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs59() {
		return bs59;
	}

	public void setBs59(double bs59) {
		this.bs59 = bs59;
	}

	private double bs60;

	@javax.persistence.Column(name = "bs60", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs60() {
		return bs60;
	}

	public void setBs60(double bs60) {
		this.bs60 = bs60;
	}

	private double bs61;

	@javax.persistence.Column(name = "bs61", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs61() {
		return bs61;
	}

	public void setBs61(double bs61) {
		this.bs61 = bs61;
	}

	private double bs62;

	@javax.persistence.Column(name = "bs62", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs62() {
		return bs62;
	}

	public void setBs62(double bs62) {
		this.bs62 = bs62;
	}

	private double bs63;

	@javax.persistence.Column(name = "bs63", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs63() {
		return bs63;
	}

	public void setBs63(double bs63) {
		this.bs63 = bs63;
	}

	private double bs64;

	@javax.persistence.Column(name = "bs64", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs64() {
		return bs64;
	}

	public void setBs64(double bs64) {
		this.bs64 = bs64;
	}

	private double bs65;

	@javax.persistence.Column(name = "bs65", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs65() {
		return bs65;
	}

	public void setBs65(double bs65) {
		this.bs65 = bs65;
	}

	private double bs66;

	@javax.persistence.Column(name = "bs66", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs66() {
		return bs66;
	}

	public void setBs66(double bs66) {
		this.bs66 = bs66;
	}

	private double bs67;

	@javax.persistence.Column(name = "bs67", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getBs67() {
		return bs67;
	}

	public void setBs67(double bs67) {
		this.bs67 = bs67;
	}

	private Stock stock;

	@javax.persistence.JoinColumn(name = "stock_id", nullable = false, insertable = true, updatable = false)
	@ManyToOne
	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		BalanceSheetNf that = (BalanceSheetNf) o;

		if (finSeason != that.finSeason) return false;
		if (finYear != that.finYear) return false;
		if (stock != null ? !stock.equals(that.stock) : that.stock != null) return false;
//		if (id != that.id) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = stock.hashCode();
		result = 31 * result + finYear;
		result = 31 * result + finSeason;
		return result;
	}
}
