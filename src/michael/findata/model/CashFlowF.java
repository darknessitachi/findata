package michael.findata.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-4-7
 * Time: ÏÂÎç4:57
 * To change this template use File | Settings | File Templates.
 */
@javax.persistence.Table(name = "cash_flow_f", schema = "", catalog = "findata")
@Entity
public class CashFlowF {
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

	private double cf01;

	@javax.persistence.Column(name = "cf01", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf01() {
		return cf01;
	}

	public void setCf01(double cf01) {
		this.cf01 = cf01;
	}

	private double cf02;

	@javax.persistence.Column(name = "cf02", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf02() {
		return cf02;
	}

	public void setCf02(double cf02) {
		this.cf02 = cf02;
	}

	private double cf03;

	@javax.persistence.Column(name = "cf03", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf03() {
		return cf03;
	}

	public void setCf03(double cf03) {
		this.cf03 = cf03;
	}

	private double cf04;

	@javax.persistence.Column(name = "cf04", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf04() {
		return cf04;
	}

	public void setCf04(double cf04) {
		this.cf04 = cf04;
	}

	private double cf05;

	@javax.persistence.Column(name = "cf05", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf05() {
		return cf05;
	}

	public void setCf05(double cf05) {
		this.cf05 = cf05;
	}

	private double cf06;

	@javax.persistence.Column(name = "cf06", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf06() {
		return cf06;
	}

	public void setCf06(double cf06) {
		this.cf06 = cf06;
	}

	private double cf07;

	@javax.persistence.Column(name = "cf07", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf07() {
		return cf07;
	}

	public void setCf07(double cf07) {
		this.cf07 = cf07;
	}

	private double cf08;

	@javax.persistence.Column(name = "cf08", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf08() {
		return cf08;
	}

	public void setCf08(double cf08) {
		this.cf08 = cf08;
	}

	private double cf09;

	@javax.persistence.Column(name = "cf09", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf09() {
		return cf09;
	}

	public void setCf09(double cf09) {
		this.cf09 = cf09;
	}

	private double cf10;

	@javax.persistence.Column(name = "cf10", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf10() {
		return cf10;
	}

	public void setCf10(double cf10) {
		this.cf10 = cf10;
	}

	private double cf11;

	@javax.persistence.Column(name = "cf11", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf11() {
		return cf11;
	}

	public void setCf11(double cf11) {
		this.cf11 = cf11;
	}

	private double cf12;

	@javax.persistence.Column(name = "cf12", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf12() {
		return cf12;
	}

	public void setCf12(double cf12) {
		this.cf12 = cf12;
	}

	private double cf13;

	@javax.persistence.Column(name = "cf13", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf13() {
		return cf13;
	}

	public void setCf13(double cf13) {
		this.cf13 = cf13;
	}

	private double cf14;

	@javax.persistence.Column(name = "cf14", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf14() {
		return cf14;
	}

	public void setCf14(double cf14) {
		this.cf14 = cf14;
	}

	private double cf15;

	@javax.persistence.Column(name = "cf15", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf15() {
		return cf15;
	}

	public void setCf15(double cf15) {
		this.cf15 = cf15;
	}

	private double cf16;

	@javax.persistence.Column(name = "cf16", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf16() {
		return cf16;
	}

	public void setCf16(double cf16) {
		this.cf16 = cf16;
	}

	private double cf17;

	@javax.persistence.Column(name = "cf17", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf17() {
		return cf17;
	}

	public void setCf17(double cf17) {
		this.cf17 = cf17;
	}

	private double cf18;

	@javax.persistence.Column(name = "cf18", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf18() {
		return cf18;
	}

	public void setCf18(double cf18) {
		this.cf18 = cf18;
	}

	private double cf19;

	@javax.persistence.Column(name = "cf19", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf19() {
		return cf19;
	}

	public void setCf19(double cf19) {
		this.cf19 = cf19;
	}

	private double cf20;

	@javax.persistence.Column(name = "cf20", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf20() {
		return cf20;
	}

	public void setCf20(double cf20) {
		this.cf20 = cf20;
	}

	private double cf21;

	@javax.persistence.Column(name = "cf21", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf21() {
		return cf21;
	}

	public void setCf21(double cf21) {
		this.cf21 = cf21;
	}

	private double cf22;

	@javax.persistence.Column(name = "cf22", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf22() {
		return cf22;
	}

	public void setCf22(double cf22) {
		this.cf22 = cf22;
	}

	private double cf23;

	@javax.persistence.Column(name = "cf23", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf23() {
		return cf23;
	}

	public void setCf23(double cf23) {
		this.cf23 = cf23;
	}

	private double cf24;

	@javax.persistence.Column(name = "cf24", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf24() {
		return cf24;
	}

	public void setCf24(double cf24) {
		this.cf24 = cf24;
	}

	private double cf25;

	@javax.persistence.Column(name = "cf25", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf25() {
		return cf25;
	}

	public void setCf25(double cf25) {
		this.cf25 = cf25;
	}

	private double cf26;

	@javax.persistence.Column(name = "cf26", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf26() {
		return cf26;
	}

	public void setCf26(double cf26) {
		this.cf26 = cf26;
	}

	private double cf27;

	@javax.persistence.Column(name = "cf27", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf27() {
		return cf27;
	}

	public void setCf27(double cf27) {
		this.cf27 = cf27;
	}

	private double cf28;

	@javax.persistence.Column(name = "cf28", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf28() {
		return cf28;
	}

	public void setCf28(double cf28) {
		this.cf28 = cf28;
	}

	private double cf29;

	@javax.persistence.Column(name = "cf29", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf29() {
		return cf29;
	}

	public void setCf29(double cf29) {
		this.cf29 = cf29;
	}

	private double cf30;

	@javax.persistence.Column(name = "cf30", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf30() {
		return cf30;
	}

	public void setCf30(double cf30) {
		this.cf30 = cf30;
	}

	private double cf31;

	@javax.persistence.Column(name = "cf31", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf31() {
		return cf31;
	}

	public void setCf31(double cf31) {
		this.cf31 = cf31;
	}

	private double cf32;

	@javax.persistence.Column(name = "cf32", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf32() {
		return cf32;
	}

	public void setCf32(double cf32) {
		this.cf32 = cf32;
	}

	private double cf33;

	@javax.persistence.Column(name = "cf33", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf33() {
		return cf33;
	}

	public void setCf33(double cf33) {
		this.cf33 = cf33;
	}

	private double cf34;

	@javax.persistence.Column(name = "cf34", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf34() {
		return cf34;
	}

	public void setCf34(double cf34) {
		this.cf34 = cf34;
	}

	private double cf35;

	@javax.persistence.Column(name = "cf35", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf35() {
		return cf35;
	}

	public void setCf35(double cf35) {
		this.cf35 = cf35;
	}

	private double cf36;

	@javax.persistence.Column(name = "cf36", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf36() {
		return cf36;
	}

	public void setCf36(double cf36) {
		this.cf36 = cf36;
	}

	private double cf37;

	@javax.persistence.Column(name = "cf37", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf37() {
		return cf37;
	}

	public void setCf37(double cf37) {
		this.cf37 = cf37;
	}

	private double cf38;

	@javax.persistence.Column(name = "cf38", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf38() {
		return cf38;
	}

	public void setCf38(double cf38) {
		this.cf38 = cf38;
	}

	private double cf39;

	@javax.persistence.Column(name = "cf39", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf39() {
		return cf39;
	}

	public void setCf39(double cf39) {
		this.cf39 = cf39;
	}

	private double cf40;

	@javax.persistence.Column(name = "cf40", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf40() {
		return cf40;
	}

	public void setCf40(double cf40) {
		this.cf40 = cf40;
	}

	private double cf41;

	@javax.persistence.Column(name = "cf41", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf41() {
		return cf41;
	}

	public void setCf41(double cf41) {
		this.cf41 = cf41;
	}

	private double cf42;

	@javax.persistence.Column(name = "cf42", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf42() {
		return cf42;
	}

	public void setCf42(double cf42) {
		this.cf42 = cf42;
	}

	private double cf43;

	@javax.persistence.Column(name = "cf43", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf43() {
		return cf43;
	}

	public void setCf43(double cf43) {
		this.cf43 = cf43;
	}

	private double cf44;

	@javax.persistence.Column(name = "cf44", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf44() {
		return cf44;
	}

	public void setCf44(double cf44) {
		this.cf44 = cf44;
	}

	private double cf45;

	@javax.persistence.Column(name = "cf45", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf45() {
		return cf45;
	}

	public void setCf45(double cf45) {
		this.cf45 = cf45;
	}

	private double cf46;

	@javax.persistence.Column(name = "cf46", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf46() {
		return cf46;
	}

	public void setCf46(double cf46) {
		this.cf46 = cf46;
	}

	private double cf47;

	@javax.persistence.Column(name = "cf47", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@Basic
	public double getCf47() {
		return cf47;
	}

	public void setCf47(double cf47) {
		this.cf47 = cf47;
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

		CashFlowF that = (CashFlowF) o;

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
