package michael.findata.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-4-7
 * Time: ÏÂÎç5:01
 * To change this template use File | Settings | File Templates.
 */
@javax.persistence.Table(name = "profit_and_loss_nf", schema = "", catalog = "findata")
@Entity
public class ProfitAndLossNf {
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

	private double pl01;

	@javax.persistence.Column(name = "pl01", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl01() {
		return pl01;
	}

	public void setPl01(double pl01) {
		this.pl01 = pl01;
	}

	private double pl02;

	@javax.persistence.Column(name = "pl02", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl02() {
		return pl02;
	}

	public void setPl02(double pl02) {
		this.pl02 = pl02;
	}

	private double pl03;

	@javax.persistence.Column(name = "pl03", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl03() {
		return pl03;
	}

	public void setPl03(double pl03) {
		this.pl03 = pl03;
	}

	private double pl04;

	@javax.persistence.Column(name = "pl04", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl04() {
		return pl04;
	}

	public void setPl04(double pl04) {
		this.pl04 = pl04;
	}

	private double pl05;

	@javax.persistence.Column(name = "pl05", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl05() {
		return pl05;
	}

	public void setPl05(double pl05) {
		this.pl05 = pl05;
	}

	private double pl06;

	@javax.persistence.Column(name = "pl06", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl06() {
		return pl06;
	}

	public void setPl06(double pl06) {
		this.pl06 = pl06;
	}

	private double pl07;

	@javax.persistence.Column(name = "pl07", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl07() {
		return pl07;
	}

	public void setPl07(double pl07) {
		this.pl07 = pl07;
	}

	private double pl08;

	@javax.persistence.Column(name = "pl08", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl08() {
		return pl08;
	}

	public void setPl08(double pl08) {
		this.pl08 = pl08;
	}

	private double pl09;

	@javax.persistence.Column(name = "pl09", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl09() {
		return pl09;
	}

	public void setPl09(double pl09) {
		this.pl09 = pl09;
	}

	private double pl10;

	@javax.persistence.Column(name = "pl10", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl10() {
		return pl10;
	}

	public void setPl10(double pl10) {
		this.pl10 = pl10;
	}

	private double pl11;

	@javax.persistence.Column(name = "pl11", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl11() {
		return pl11;
	}

	public void setPl11(double pl11) {
		this.pl11 = pl11;
	}

	private double pl12;

	@javax.persistence.Column(name = "pl12", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl12() {
		return pl12;
	}

	public void setPl12(double pl12) {
		this.pl12 = pl12;
	}

	private double pl13;

	@javax.persistence.Column(name = "pl13", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl13() {
		return pl13;
	}

	public void setPl13(double pl13) {
		this.pl13 = pl13;
	}

	private double pl14;

	@javax.persistence.Column(name = "pl14", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl14() {
		return pl14;
	}

	public void setPl14(double pl14) {
		this.pl14 = pl14;
	}

	private double pl15;

	@javax.persistence.Column(name = "pl15", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl15() {
		return pl15;
	}

	public void setPl15(double pl15) {
		this.pl15 = pl15;
	}

	private double pl16;

	@javax.persistence.Column(name = "pl16", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl16() {
		return pl16;
	}

	public void setPl16(double pl16) {
		this.pl16 = pl16;
	}

	private double pl17;

	@javax.persistence.Column(name = "pl17", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl17() {
		return pl17;
	}

	public void setPl17(double pl17) {
		this.pl17 = pl17;
	}

	private double pl18;

	@javax.persistence.Column(name = "pl18", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl18() {
		return pl18;
	}

	public void setPl18(double pl18) {
		this.pl18 = pl18;
	}

	private double pl19;

	@javax.persistence.Column(name = "pl19", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl19() {
		return pl19;
	}

	public void setPl19(double pl19) {
		this.pl19 = pl19;
	}

	private double pl20;

	@javax.persistence.Column(name = "pl20", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl20() {
		return pl20;
	}

	public void setPl20(double pl20) {
		this.pl20 = pl20;
	}

	private double pl21;

	@javax.persistence.Column(name = "pl21", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl21() {
		return pl21;
	}

	public void setPl21(double pl21) {
		this.pl21 = pl21;
	}

	private double pl22;

	@javax.persistence.Column(name = "pl22", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl22() {
		return pl22;
	}

	public void setPl22(double pl22) {
		this.pl22 = pl22;
	}

	private double pl23;

	@javax.persistence.Column(name = "pl23", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl23() {
		return pl23;
	}

	public void setPl23(double pl23) {
		this.pl23 = pl23;
	}

	private double pl24;

	@javax.persistence.Column(name = "pl24", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl24() {
		return pl24;
	}

	public void setPl24(double pl24) {
		this.pl24 = pl24;
	}

	private double pl25;

	@javax.persistence.Column(name = "pl25", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl25() {
		return pl25;
	}

	public void setPl25(double pl25) {
		this.pl25 = pl25;
	}

	private double pl26;

	@javax.persistence.Column(name = "pl26", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl26() {
		return pl26;
	}

	public void setPl26(double pl26) {
		this.pl26 = pl26;
	}

	private double pl27;

	@javax.persistence.Column(name = "pl27", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getPl27() {
		return pl27;
	}

	public void setPl27(double pl27) {
		this.pl27 = pl27;
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

		ProfitAndLossNf that = (ProfitAndLossNf) o;

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
