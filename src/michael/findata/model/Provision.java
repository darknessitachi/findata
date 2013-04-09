package michael.findata.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-4-7
 * Time: ÏÂÎç5:01
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class Provision {
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

	private double pv01;

	@javax.persistence.Column(name = "pv01", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPv01() {
		return pv01;
	}

	public void setPv01(double pv01) {
		this.pv01 = pv01;
	}

	private double pv02;

	@javax.persistence.Column(name = "pv02", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPv02() {
		return pv02;
	}

	public void setPv02(double pv02) {
		this.pv02 = pv02;
	}

	private double pv03;

	@javax.persistence.Column(name = "pv03", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPv03() {
		return pv03;
	}

	public void setPv03(double pv03) {
		this.pv03 = pv03;
	}

	private double pv04;

	@javax.persistence.Column(name = "pv04", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPv04() {
		return pv04;
	}

	public void setPv04(double pv04) {
		this.pv04 = pv04;
	}

	private double pv05;

	@javax.persistence.Column(name = "pv05", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPv05() {
		return pv05;
	}

	public void setPv05(double pv05) {
		this.pv05 = pv05;
	}

	private double pv06;

	@javax.persistence.Column(name = "pv06", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPv06() {
		return pv06;
	}

	public void setPv06(double pv06) {
		this.pv06 = pv06;
	}

	private double pv07;

	@javax.persistence.Column(name = "pv07", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPv07() {
		return pv07;
	}

	public void setPv07(double pv07) {
		this.pv07 = pv07;
	}

	private double pv08;

	@javax.persistence.Column(name = "pv08", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPv08() {
		return pv08;
	}

	public void setPv08(double pv08) {
		this.pv08 = pv08;
	}

	private double pv09;

	@javax.persistence.Column(name = "pv09", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPv09() {
		return pv09;
	}

	public void setPv09(double pv09) {
		this.pv09 = pv09;
	}

	private double pv10;

	@javax.persistence.Column(name = "pv10", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPv10() {
		return pv10;
	}

	public void setPv10(double pv10) {
		this.pv10 = pv10;
	}

	private double pv11;

	@javax.persistence.Column(name = "pv11", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPv11() {
		return pv11;
	}

	public void setPv11(double pv11) {
		this.pv11 = pv11;
	}

	private double pv12;

	@javax.persistence.Column(name = "pv12", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPv12() {
		return pv12;
	}

	public void setPv12(double pv12) {
		this.pv12 = pv12;
	}

	private double pv13;

	@javax.persistence.Column(name = "pv13", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPv13() {
		return pv13;
	}

	public void setPv13(double pv13) {
		this.pv13 = pv13;
	}

	private double pv14;

	@javax.persistence.Column(name = "pv14", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPv14() {
		return pv14;
	}

	public void setPv14(double pv14) {
		this.pv14 = pv14;
	}

	private double pv15;

	@javax.persistence.Column(name = "pv15", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPv15() {
		return pv15;
	}

	public void setPv15(double pv15) {
		this.pv15 = pv15;
	}

	private double pv16;

	@javax.persistence.Column(name = "pv16", nullable = true, insertable = true, updatable = true, length = 22, precision = 0)
	@javax.persistence.Basic
	public double getPv16() {
		return pv16;
	}

	public void setPv16(double pv16) {
		this.pv16 = pv16;
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

		Provision that = (Provision) o;

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
