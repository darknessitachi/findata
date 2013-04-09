package michael.findata.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-4-7
 * Time: ÏÂÎç5:00
 * To change this template use File | Settings | File Templates.
 */
@javax.persistence.Table(name = "profit_and_loss_f", schema = "", catalog = "findata")
@Entity
public class ProfitAndLossF {
	private int id;

	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	@javax.persistence.Column(name = "id", nullable = false, insertable = true, updatable = true, length = 10, precision = 0)
	@javax.persistence.Id
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private int finYear;

	@javax.persistence.Column(name = "fin_year", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@javax.persistence.Basic
	public int getFinYear() {
		return finYear;
	}

	public void setFinYear(int finYear) {
		this.finYear = finYear;
	}

	private int finSeason;

	@javax.persistence.Column(name = "fin_season", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@javax.persistence.Basic
	public int getFinSeason() {
		return finSeason;
	}

	public void setFinSeason(int finSeason) {
		this.finSeason = finSeason;
	}

	private double pl01;

	@javax.persistence.Column(name = "pl01", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl01() {
		return pl01;
	}

	public void setPl01(double pl01) {
		this.pl01 = pl01;
	}

	private double pl02;

	@javax.persistence.Column(name = "pl02", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl02() {
		return pl02;
	}

	public void setPl02(double pl02) {
		this.pl02 = pl02;
	}

	private double pl03;

	@javax.persistence.Column(name = "pl03", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl03() {
		return pl03;
	}

	public void setPl03(double pl03) {
		this.pl03 = pl03;
	}

	private double pl04;

	@javax.persistence.Column(name = "pl04", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl04() {
		return pl04;
	}

	public void setPl04(double pl04) {
		this.pl04 = pl04;
	}

	private double pl05;

	@javax.persistence.Column(name = "pl05", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl05() {
		return pl05;
	}

	public void setPl05(double pl05) {
		this.pl05 = pl05;
	}

	private double pl06;

	@javax.persistence.Column(name = "pl06", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl06() {
		return pl06;
	}

	public void setPl06(double pl06) {
		this.pl06 = pl06;
	}

	private double pl07;

	@javax.persistence.Column(name = "pl07", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl07() {
		return pl07;
	}

	public void setPl07(double pl07) {
		this.pl07 = pl07;
	}

	private double pl08;

	@javax.persistence.Column(name = "pl08", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl08() {
		return pl08;
	}

	public void setPl08(double pl08) {
		this.pl08 = pl08;
	}

	private double pl09;

	@javax.persistence.Column(name = "pl09", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl09() {
		return pl09;
	}

	public void setPl09(double pl09) {
		this.pl09 = pl09;
	}

	private double pl10;

	@javax.persistence.Column(name = "pl10", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl10() {
		return pl10;
	}

	public void setPl10(double pl10) {
		this.pl10 = pl10;
	}

	private double pl11;

	@javax.persistence.Column(name = "pl11", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl11() {
		return pl11;
	}

	public void setPl11(double pl11) {
		this.pl11 = pl11;
	}

	private double pl12;

	@javax.persistence.Column(name = "pl12", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl12() {
		return pl12;
	}

	public void setPl12(double pl12) {
		this.pl12 = pl12;
	}

	private double pl13;

	@javax.persistence.Column(name = "pl13", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl13() {
		return pl13;
	}

	public void setPl13(double pl13) {
		this.pl13 = pl13;
	}

	private double pl14;

	@javax.persistence.Column(name = "pl14", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl14() {
		return pl14;
	}

	public void setPl14(double pl14) {
		this.pl14 = pl14;
	}

	private double pl15;

	@javax.persistence.Column(name = "pl15", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl15() {
		return pl15;
	}

	public void setPl15(double pl15) {
		this.pl15 = pl15;
	}

	private double pl16;

	@javax.persistence.Column(name = "pl16", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl16() {
		return pl16;
	}

	public void setPl16(double pl16) {
		this.pl16 = pl16;
	}

	private double pl17;

	@javax.persistence.Column(name = "pl17", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl17() {
		return pl17;
	}

	public void setPl17(double pl17) {
		this.pl17 = pl17;
	}

	private double pl18;

	@javax.persistence.Column(name = "pl18", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl18() {
		return pl18;
	}

	public void setPl18(double pl18) {
		this.pl18 = pl18;
	}

	private double pl19;

	@javax.persistence.Column(name = "pl19", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl19() {
		return pl19;
	}

	public void setPl19(double pl19) {
		this.pl19 = pl19;
	}

	private double pl20;

	@javax.persistence.Column(name = "pl20", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl20() {
		return pl20;
	}

	public void setPl20(double pl20) {
		this.pl20 = pl20;
	}

	private double pl21;

	@javax.persistence.Column(name = "pl21", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl21() {
		return pl21;
	}

	public void setPl21(double pl21) {
		this.pl21 = pl21;
	}

	private double pl22;

	@javax.persistence.Column(name = "pl22", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl22() {
		return pl22;
	}

	public void setPl22(double pl22) {
		this.pl22 = pl22;
	}

	private double pl23;

	@javax.persistence.Column(name = "pl23", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl23() {
		return pl23;
	}

	public void setPl23(double pl23) {
		this.pl23 = pl23;
	}

	private double pl24;

	@javax.persistence.Column(name = "pl24", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl24() {
		return pl24;
	}

	public void setPl24(double pl24) {
		this.pl24 = pl24;
	}

	private double pl25;

	@javax.persistence.Column(name = "pl25", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl25() {
		return pl25;
	}

	public void setPl25(double pl25) {
		this.pl25 = pl25;
	}

	private double pl26;

	@javax.persistence.Column(name = "pl26", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl26() {
		return pl26;
	}

	public void setPl26(double pl26) {
		this.pl26 = pl26;
	}

	private double pl27;

	@javax.persistence.Column(name = "pl27", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl27() {
		return pl27;
	}

	public void setPl27(double pl27) {
		this.pl27 = pl27;
	}

	private double pl28;

	@javax.persistence.Column(name = "pl28", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl28() {
		return pl28;
	}

	public void setPl28(double pl28) {
		this.pl28 = pl28;
	}

	private double pl29;

	@javax.persistence.Column(name = "pl29", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl29() {
		return pl29;
	}

	public void setPl29(double pl29) {
		this.pl29 = pl29;
	}

	private double pl30;

	@javax.persistence.Column(name = "pl30", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl30() {
		return pl30;
	}

	public void setPl30(double pl30) {
		this.pl30 = pl30;
	}

	private double pl31;

	@javax.persistence.Column(name = "pl31", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl31() {
		return pl31;
	}

	public void setPl31(double pl31) {
		this.pl31 = pl31;
	}

	private double pl32;

	@javax.persistence.Column(name = "pl32", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl32() {
		return pl32;
	}

	public void setPl32(double pl32) {
		this.pl32 = pl32;
	}

	private double pl33;

	@javax.persistence.Column(name = "pl33", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl33() {
		return pl33;
	}

	public void setPl33(double pl33) {
		this.pl33 = pl33;
	}

	private double pl34;

	@javax.persistence.Column(name = "pl34", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl34() {
		return pl34;
	}

	public void setPl34(double pl34) {
		this.pl34 = pl34;
	}

	private double pl35;

	@javax.persistence.Column(name = "pl35", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl35() {
		return pl35;
	}

	public void setPl35(double pl35) {
		this.pl35 = pl35;
	}

	private double pl36;

	@javax.persistence.Column(name = "pl36", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl36() {
		return pl36;
	}

	public void setPl36(double pl36) {
		this.pl36 = pl36;
	}

	private double pl37;

	@javax.persistence.Column(name = "pl37", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl37() {
		return pl37;
	}

	public void setPl37(double pl37) {
		this.pl37 = pl37;
	}

	private double pl38;

	@javax.persistence.Column(name = "pl38", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl38() {
		return pl38;
	}

	public void setPl38(double pl38) {
		this.pl38 = pl38;
	}

	private double pl39;

	@javax.persistence.Column(name = "pl39", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl39() {
		return pl39;
	}

	public void setPl39(double pl39) {
		this.pl39 = pl39;
	}

	private double pl40;

	@javax.persistence.Column(name = "pl40", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl40() {
		return pl40;
	}

	public void setPl40(double pl40) {
		this.pl40 = pl40;
	}

	private double pl41;

	@javax.persistence.Column(name = "pl41", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl41() {
		return pl41;
	}

	public void setPl41(double pl41) {
		this.pl41 = pl41;
	}

	private double pl42;

	@javax.persistence.Column(name = "pl42", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl42() {
		return pl42;
	}

	public void setPl42(double pl42) {
		this.pl42 = pl42;
	}

	private double pl43;

	@javax.persistence.Column(name = "pl43", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl43() {
		return pl43;
	}

	public void setPl43(double pl43) {
		this.pl43 = pl43;
	}

	private double pl44;

	@javax.persistence.Column(name = "pl44", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl44() {
		return pl44;
	}

	public void setPl44(double pl44) {
		this.pl44 = pl44;
	}

	private double pl45;

	@javax.persistence.Column(name = "pl45", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl45() {
		return pl45;
	}

	public void setPl45(double pl45) {
		this.pl45 = pl45;
	}

	private double pl46;

	@javax.persistence.Column(name = "pl46", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl46() {
		return pl46;
	}

	public void setPl46(double pl46) {
		this.pl46 = pl46;
	}

	private double pl47;

	@javax.persistence.Column(name = "pl47", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPl47() {
		return pl47;
	}

	public void setPl47(double pl47) {
		this.pl47 = pl47;
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

		ProfitAndLossF that = (ProfitAndLossF) o;

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
