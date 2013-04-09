package michael.findata.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;

@javax.persistence.Table(name = "stock_price", schema = "", catalog = "findata")
@Entity
public class StockPrice {
	private long id;

	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	@javax.persistence.Column(name = "id", nullable = false, insertable = true, updatable = true, length = 20, precision = 0)
	@Id
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
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

	private Timestamp date;

	@javax.persistence.Column(name = "date", nullable = false, insertable = true, updatable = true, length = 19, precision = 0)
	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	private int open;

	@javax.persistence.Column(name = "open", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	public int getOpen() {
		return open;
	}

	public void setOpen(int open) {
		this.open = open;
	}

	private int high;

	@javax.persistence.Column(name = "high", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	public int getHigh() {
		return high;
	}

	public void setHigh(int high) {
		this.high = high;
	}

	private int low;

	@javax.persistence.Column(name = "low", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	public int getLow() {
		return low;
	}

	public void setLow(int low) {
		this.low = low;
	}

	private int close;

	@javax.persistence.Column(name = "close", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	public int getClose() {
		return close;
	}

	public void setClose(int close) {
		this.close = close;
	}

	private int avg;

	@javax.persistence.Column(name = "avg", nullable = true, insertable = true, updatable = true, length = 10, precision = 0)
	@Basic
	public int getAvg() {
		return avg;
	}

	public void setAvg(int avg) {
		this.avg = avg;
	}

	private Float adjustmentFactor;

	@javax.persistence.Column(name = "adjustment_factor", nullable = true, insertable = true, updatable = true, length = 12, precision = 0)
	@Basic
	public Float getAdjustmentFactor() {
		return adjustmentFactor;
	}

	public void setAdjustmentFactor(Float adjustmentFactor) {
		this.adjustmentFactor = adjustmentFactor;
	}

	private Float epLast4Seasons;

	@javax.persistence.Column(name = "ep_last_4_seasons", nullable = true, insertable = true, updatable = true, length = 12, precision = 0)
	@Basic
	public Float getEpLast4Seasons() {
		return epLast4Seasons;
	}

	public void setEpLast4Seasons(Float epLast4Seasons) {
		this.epLast4Seasons = epLast4Seasons;
	}

	private Float epL4SMax;

	@javax.persistence.Column(name = "ep_l4s_max", nullable = true, insertable = true, updatable = true, length = 12, precision = 0)
	@Basic
	public Float getEpL4SMax() {
		return epL4SMax;
	}

	public void setEpL4SMax(Float epL4SMax) {
		this.epL4SMax = epL4SMax;
	}

	private Float epL4SMin;

	@javax.persistence.Column(name = "ep_l4s_min", nullable = true, insertable = true, updatable = true, length = 12, precision = 0)
	@Basic
	public Float getEpL4SMin() {
		return epL4SMin;
	}

	public void setEpL4SMin(Float epL4SMin) {
		this.epL4SMin = epL4SMin;
	}

	private Float eb;

	@javax.persistence.Column(name = "eb", nullable = true, insertable = true, updatable = true, length = 12, precision = 0)
	@Basic
	public Float getEb() {
		return eb;
	}

	public void setEb(Float eb) {
		this.eb = eb;
	}

	private Float ebMax;

	@javax.persistence.Column(name = "eb_max", nullable = true, insertable = true, updatable = true, length = 12, precision = 0)
	@Basic
	public Float getEbMax() {
		return ebMax;
	}

	public void setEbMax(Float ebMax) {
		this.ebMax = ebMax;
	}

	private Float ebMin;

	@javax.persistence.Column(name = "eb_min", nullable = true, insertable = true, updatable = true, length = 12, precision = 0)
	@Basic
	public Float getEbMin() {
		return ebMin;
	}

	public void setEbMin(Float ebMin) {
		this.ebMin = ebMin;
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
