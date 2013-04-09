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
@javax.persistence.Table(name = "share_number_change", schema = "", catalog = "findata")
@Entity
public class ShareNumberChange {
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

	private Date changeDate;

	@javax.persistence.Column(name = "change_date", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@javax.persistence.Basic
	public Date getChangeDate() {
		return changeDate;
	}

	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}

	private long numberOfShares;

	@javax.persistence.Column(name = "number_of_shares", nullable = true, insertable = true, updatable = true, length = 19, precision = 0)
	@javax.persistence.Basic
	public long getNumberOfShares() {
		return numberOfShares;
	}

	public void setNumberOfShares(long numberOfShares) {
		this.numberOfShares = numberOfShares;
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

		ShareNumberChange that = (ShareNumberChange) o;

//		if (id != that.id) return false;
		if (stock != null ? !stock.equals(that.stock) : that.stock != null) return false;
		if (changeDate != null ? !changeDate.equals(that.changeDate) : that.changeDate != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = stock.hashCode();
		result = 31 * result + (changeDate != null ? changeDate.hashCode() : 0);
		return result;
	}
}
