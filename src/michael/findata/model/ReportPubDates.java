package michael.findata.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import java.sql.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-4-7
 * Time: ÏÂÎç5:01
 * To change this template use File | Settings | File Templates.
 */
@javax.persistence.Table(name = "report_pub_dates", schema = "", catalog = "findata")
@Entity
public class ReportPubDates {
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

	private Date finDate;

	@javax.persistence.Column(name = "fin_date", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@javax.persistence.Basic
	public Date getFinDate() {
		return finDate;
	}

	public void setFinDate(Date finDate) {
		this.finDate = finDate;
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

		ReportPubDates that = (ReportPubDates) o;

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