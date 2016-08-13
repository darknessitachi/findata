package michael.findata.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "stock_price")
public class StockPrice {

	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	@Column(name = "id", nullable = false, insertable = true, updatable = true, length = 20, precision = 0)
	@Id
	private long id;

	@JoinColumn(name = "stock_id", nullable = false, insertable = true, updatable = false)
	@ManyToOne
	private Stock stock;

	@Column(name = "date", nullable = false, insertable = true, updatable = true, length = 19, precision = 0)
	private Timestamp date;

	@Column(name = "open", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	private int open;

	@Column(name = "high", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	private int high;

	@Column(name = "low", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	private int low;

	@Column(name = "close", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	private int close;

	@Column(name = "avg", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	private int avg;

	@Column(name = "adjustment_factor", nullable = true, insertable = true, updatable = true, length = 12, precision = 0)
	@Basic
	private Float adjustmentFactor;

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}


	public Stock getStock() {
		return stock;
	}
	public void setStock(Stock stock) {
		this.stock = stock;
	}


	public Timestamp getDate() {
		return date;
	}
	public void setDate(Timestamp date) {
		this.date = date;
	}


	public int getOpen() {
		return open;
	}
	public void setOpen(int open) {
		this.open = open;
	}

	public int getHigh() {
		return high;
	}
	public void setHigh(int high) {
		this.high = high;
	}

	public int getLow() {
		return low;
	}
	public void setLow(int low) {
		this.low = low;
	}


	public int getClose() {
		return close;
	}
	public void setClose(int close) {
		this.close = close;
	}


	public int getAvg() {
		return avg;
	}
	public void setAvg(int avg) {
		this.avg = avg;
	}


	public Float getAdjustmentFactor() {
		return adjustmentFactor;
	}
	public void setAdjustmentFactor(Float adjustmentFactor) {
		this.adjustmentFactor = adjustmentFactor;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		StockPrice that = (StockPrice) o;

//		if (id != that.id) return false;
		if (stock != null ? !stock.equals(that.stock) : that.stock != null) return false;
		if (date != null ? !date.equals(that.date) : that.date != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = stock.hashCode();
		result = 31 * result + (date != null ? date.hashCode() : 0);
		return result;
	}
}